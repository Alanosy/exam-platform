package org.dromara.exam.cert.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.exam.answer.api.RemoteExamAnswerService;
import org.dromara.exam.answer.api.domain.RemoteRecordVo;
import org.dromara.exam.cert.api.domain.RemoteCertIssueBo;
import org.dromara.exam.cert.api.domain.RemoteCertVo;
import org.dromara.exam.cert.domain.Certificate;
import org.dromara.exam.cert.domain.CertificateRecord;
import org.dromara.exam.cert.domain.ExamRef;
import org.dromara.exam.cert.domain.bo.CertIssueBo;
import org.dromara.exam.cert.domain.bo.CertificateBo;
import org.dromara.exam.cert.domain.bo.CertificateRecordBo;
import org.dromara.exam.cert.domain.vo.CertificateOptionVo;
import org.dromara.exam.cert.domain.vo.CertificateRecordVo;
import org.dromara.exam.cert.domain.vo.CertificateVo;
import org.dromara.exam.cert.mapper.CertificateMapper;
import org.dromara.exam.cert.mapper.CertificateRecordMapper;
import org.dromara.exam.cert.mapper.ExamRefMapper;
import org.dromara.exam.cert.service.ICertService;
import org.dromara.resource.api.RemoteFileService;
import org.dromara.system.api.RemoteUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 证书服务实现
 *
 * <p>两条颁发路径，都走同一个 {@link #issueOnPass(RemoteCertIssueBo)}：
 *   1. 交卷后（客观题卷，成绩已定）由答题服务调用
 *   2. 阅卷完成后（含主观题）由答题服务调用
 *   3. 管理端手工补发：成绩由本服务拿 recordId 去答题服务拉，不靠前端填
 *
 * <p>幂等：同一份答卷（recordId）只会有一张证书，重复调用直接把原证书返回。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertServiceImpl implements ICertService {

    @DubboReference
    private RemoteFileService remoteFileService;

    @DubboReference
    private RemoteExamAnswerService remoteExamAnswerService;

    @DubboReference
    private RemoteUserService remoteUserService;

    private final CertificateMapper certificateMapper;
    private final CertificateRecordMapper recordMapper;
    private final ExamRefMapper examRefMapper;

    /* --------------------------------- 证书模板 --------------------------------- */

    @Override
    public TableDataInfo<CertificateVo> listPage(CertificateBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Certificate> lqw = Wrappers.lambdaQuery(Certificate.class);
        lqw.and(StringUtils.isNotBlank(bo.getKeyword()), w -> w
            .like(Certificate::getCertName, bo.getKeyword())
            .or()
            .like(Certificate::getCertCode, bo.getKeyword()));
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), Certificate::getStatus, bo.getStatus());
        lqw.orderByDesc(Certificate::getCreateTime);
        Page<CertificateVo> page = certificateMapper.selectVoPage(pageQuery.build(), lqw);
        fillCertUrl(page.getRecords());
        return TableDataInfo.build(page);
    }

    @Override
    public CertificateVo queryById(Long id) {
        CertificateVo vo = certificateMapper.selectVoById(id);
        if (ObjectUtil.isNotNull(vo)) {
            fillCertUrl(List.of(vo));
        }
        return vo;
    }

    @Override
    public List<CertificateOptionVo> options() {
        List<Certificate> list = certificateMapper.selectList(
            Wrappers.lambdaQuery(Certificate.class)
                .eq(Certificate::getStatus, Certificate.STATUS_NORMAL)
                .orderByDesc(Certificate::getCreateTime));
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(item -> {
            CertificateOptionVo vo = new CertificateOptionVo();
            vo.setId(item.getId());
            vo.setCertName(item.getCertName());
            vo.setCertCode(item.getCertCode());
            vo.setTitle(item.getTitle());
            vo.setValidType(item.getValidType());
            vo.setValidDays(item.getValidDays());
            return vo;
        }).toList();
    }

    @Override
    public Long insert(CertificateBo bo) {
        checkNameRepeat(bo.getCertName(), null);
        Certificate cert = MapstructUtils.convert(bo, Certificate.class);
        cert.setId(null);
        cert.setIssueCount(0);
        cert.setStatus(StringUtils.defaultIfBlank(bo.getStatus(), Certificate.STATUS_NORMAL));
        cert.setOrientation(StringUtils.defaultIfBlank(bo.getOrientation(), Certificate.ORIENTATION_LAND));
        cert.setValidType(StringUtils.defaultIfBlank(bo.getValidType(), Certificate.VALID_FOREVER));
        cert.setValidDays(ObjectUtil.defaultIfNull(bo.getValidDays(), 0));
        cert.setBgColor(StringUtils.defaultIfBlank(bo.getBgColor(), "#fdfaf3"));
        cert.setTenantId(TenantHelper.getTenantId());
        certificateMapper.insert(cert);
        return cert.getId();
    }

    @Override
    public void update(CertificateBo bo) {
        if (ObjectUtil.isNull(bo.getId())) {
            throw new ServiceException("证书模板ID不能为空");
        }
        checkNameRepeat(bo.getCertName(), bo.getId());
        Certificate db = certificateMapper.selectById(bo.getId());
        if (ObjectUtil.isNull(db)) {
            throw new ServiceException("证书模板不存在或已删除");
        }
        Certificate cert = MapstructUtils.convert(bo, Certificate.class);
        // 已颁发数量是颁发时累加的，表单不带这个字段，不能让它被覆盖成 null
        cert.setIssueCount(db.getIssueCount());
        cert.setTenantId(db.getTenantId());
        certificateMapper.updateById(cert);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        certificateMapper.deleteByIds(ids);
    }

    /* -------------------------------- 颁发记录 -------------------------------- */

    @Override
    public TableDataInfo<CertificateRecordVo> listRecordPage(CertificateRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CertificateRecord> lqw = Wrappers.lambdaQuery(CertificateRecord.class);
        lqw.eq(ObjectUtil.isNotNull(bo.getCertId()), CertificateRecord::getCertId, bo.getCertId());
        lqw.eq(ObjectUtil.isNotNull(bo.getExamId()), CertificateRecord::getExamId, bo.getExamId());
        lqw.and(StringUtils.isNotBlank(bo.getKeyword()), w -> w
            .like(CertificateRecord::getNickName, bo.getKeyword())
            .or()
            .like(CertificateRecord::getAccount, bo.getKeyword())
            .or()
            .like(CertificateRecord::getCertNo, bo.getKeyword()));
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), CertificateRecord::getStatus, bo.getStatus());
        lqw.orderByDesc(CertificateRecord::getIssueTime, CertificateRecord::getId);
        Page<CertificateRecordVo> page = recordMapper.selectVoPage(pageQuery.build(), lqw);
        fillRecordDisplay(page.getRecords());
        return TableDataInfo.build(page);
    }

    @Override
    public List<CertificateRecordVo> listMine() {
        Long userId = LoginHelper.getUserId();
        List<CertificateRecord> list = recordMapper.selectList(
            Wrappers.lambdaQuery(CertificateRecord.class)
                .eq(CertificateRecord::getUserId, userId)
                .orderByDesc(CertificateRecord::getIssueTime));
        List<CertificateRecordVo> voList = new ArrayList<>();
        for (CertificateRecord item : list) {
            voList.add(MapstructUtils.convert(item, CertificateRecordVo.class));
        }
        fillRecordDisplay(voList);
        return voList;
    }

    @Override
    public CertificateRecordVo queryMineByRecord(Long recordId) {
        if (ObjectUtil.isNull(recordId)) {
            return null;
        }
        CertificateRecord record = recordMapper.selectOne(
            Wrappers.lambdaQuery(CertificateRecord.class)
                .eq(CertificateRecord::getRecordId, recordId)
                .eq(CertificateRecord::getUserId, LoginHelper.getUserId()));
        if (ObjectUtil.isNull(record)) {
            return null;
        }
        CertificateRecordVo vo = MapstructUtils.convert(record, CertificateRecordVo.class);
        fillRecordDisplay(List.of(vo));
        return vo;
    }

    @Override
    public void revoke(Long id, String reason) {
        CertificateRecord record = recordMapper.selectById(id);
        if (ObjectUtil.isNull(record)) {
            throw new ServiceException("证书不存在或已删除");
        }
        if (CertificateRecord.STATUS_REVOKED.equals(record.getStatus())) {
            return;
        }
        CertificateRecord update = new CertificateRecord();
        update.setId(id);
        update.setStatus(CertificateRecord.STATUS_REVOKED);
        update.setRevokeReason(StringUtils.defaultIfBlank(reason, "管理员吊销"));
        update.setRevokeTime(new Date());
        recordMapper.updateById(update);
    }

    @Override
    public Long countByExamId(Long examId) {
        if (ObjectUtil.isNull(examId)) {
            return 0L;
        }
        Long count = recordMapper.selectCount(
            Wrappers.lambdaQuery(CertificateRecord.class)
                .eq(CertificateRecord::getExamId, examId)
                .eq(CertificateRecord::getStatus, CertificateRecord.STATUS_VALID));
        return ObjectUtil.defaultIfNull(count, 0L);
    }

    /* --------------------------------- 颁发证书 --------------------------------- */

    @Override
    public RemoteCertVo issueOnPass(RemoteCertIssueBo bo) {
        if (ObjectUtil.isNull(bo) || ObjectUtil.isNull(bo.getExamId()) || ObjectUtil.isNull(bo.getRecordId())) {
            return null;
        }
        // 没及格不发；答卷ID为空说明这份卷子还没交，更不发
        if (!Boolean.TRUE.equals(bo.getPassed())) {
            return null;
        }
        // 幂等：同一份答卷只发一张，重考是另一份答卷会另发一张
        CertificateRecord exist = recordMapper.selectOne(
            Wrappers.lambdaQuery(CertificateRecord.class).eq(CertificateRecord::getRecordId, bo.getRecordId()));
        if (ObjectUtil.isNotNull(exist)) {
            return toRemote(exist);
        }
        Long certId = certIdOfExam(bo.getExamId());
        if (ObjectUtil.isNull(certId)) {
            return null;
        }
        Certificate cert = certificateMapper.selectById(certId);
        if (ObjectUtil.isNull(cert) || !Certificate.STATUS_NORMAL.equals(cert.getStatus())) {
            // 模板被删 / 被停用就不发：宁可没有，也不能发一张内容对不上的证书
            log.warn("证书模板不可用，跳过颁发 examId={}, certId={}", bo.getExamId(), certId);
            return null;
        }
        Date issueTime = ObjectUtil.defaultIfNull(bo.getSubmitTime(), new Date());
        Date expireTime = expireTimeOf(cert, issueTime);
        String certNo = newCertNo();

        CertificateRecord record = new CertificateRecord();
        record.setCertId(cert.getId());
        record.setCertNo(certNo);
        record.setExamId(bo.getExamId());
        record.setExamName(StringUtils.defaultIfBlank(bo.getExamName(), examNameOf(bo.getExamId())));
        record.setRecordId(bo.getRecordId());
        record.setUserId(bo.getUserId());
        record.setAccount(StringUtils.defaultIfBlank(bo.getAccount(), ""));
        // 答题服务只认得账号，姓名要问用户服务；证书上不写名字会很奇怪，所以这里兜底查一次
        record.setNickName(StringUtils.defaultIfBlank(bo.getNickName(), nickNameOf(bo.getUserId())));
        record.setAttemptNo(ObjectUtil.defaultIfNull(bo.getAttemptNo(), 1));
        record.setScore(nz(bo.getScore()));
        record.setPassScore(nz(bo.getPassScore()));
        record.setTotalScore(nz(bo.getTotalScore()));
        // 正文 / 样式全部快照：模板以后怎么改，这张证书都不跟着变
        record.setTitle(StringUtils.defaultIfBlank(cert.getTitle(), ""));
        record.setSubtitle(StringUtils.defaultIfBlank(cert.getSubtitle(), ""));
        record.setContent(render(cert.getContent(), bo, certNo, expireTime));
        record.setIssuer(StringUtils.defaultIfBlank(cert.getIssuer(), ""));
        record.setSealOssId(StringUtils.defaultIfBlank(cert.getSealOssId(), ""));
        record.setBgOssId(StringUtils.defaultIfBlank(cert.getBgOssId(), ""));
        record.setBgColor(StringUtils.defaultIfBlank(cert.getBgColor(), "#fdfaf3"));
        record.setOrientation(StringUtils.defaultIfBlank(cert.getOrientation(), Certificate.ORIENTATION_LAND));
        record.setIssueType(CertificateRecord.ISSUE_AUTO);
        record.setIssueTime(issueTime);
        record.setExpireTime(expireTime);
        record.setStatus(CertificateRecord.STATUS_VALID);
        record.setTenantId(ObjectUtil.defaultIfNull(bo.getTenantId(), TenantHelper.getTenantId()));
        recordMapper.insert(record);

        certificateMapper.update(null, Wrappers.lambdaUpdate(Certificate.class)
            .setSql("issue_count = issue_count + 1")
            .eq(Certificate::getId, cert.getId()));
        return toRemote(record);
    }

    @Override
    public RemoteCertVo issueManual(CertIssueBo bo) {
        if (ObjectUtil.isNull(bo) || ObjectUtil.isNull(bo.getRecordId())) {
            throw new ServiceException("请选择要补发证书的答卷");
        }
        RemoteRecordVo recordVo = remoteExamAnswerService.queryRecord(bo.getRecordId());
        if (ObjectUtil.isNull(recordVo)) {
            throw new ServiceException("答卷不存在，无法补发");
        }
        if (!ObjectUtil.equal(1L, recordVo.getPassed())) {
            throw new ServiceException("该考生未及格，不能颁发证书");
        }
        CertificateRecord exist = recordMapper.selectOne(
            Wrappers.lambdaQuery(CertificateRecord.class).eq(CertificateRecord::getRecordId, bo.getRecordId()));
        if (ObjectUtil.isNotNull(exist)) {
            throw new ServiceException("该答卷已颁发过证书：" + exist.getCertNo());
        }
        Long examId = ObjectUtil.defaultIfNull(bo.getExamId(), recordVo.getExamId());
        RemoteCertIssueBo issueBo = new RemoteCertIssueBo();
        issueBo.setExamId(examId);
        issueBo.setExamName(examNameOf(examId));
        issueBo.setRecordId(recordVo.getRecordId());
        issueBo.setUserId(recordVo.getUserId());
        issueBo.setAccount(recordVo.getAccount());
        issueBo.setNickName(nickNameOf(recordVo.getUserId()));
        issueBo.setAttemptNo(recordVo.getAttemptNo());
        issueBo.setScore(recordVo.getTotalScore());
        issueBo.setPassScore(recordVo.getPassScore());
        issueBo.setTotalScore(recordVo.getTotalScore());
        issueBo.setPassed(Boolean.TRUE);
        issueBo.setSubmitTime(ObjectUtil.defaultIfNull(recordVo.getSubmitTime(), new Date()));
        issueBo.setTenantId(TenantHelper.getTenantId());
        RemoteCertVo vo = issueOnPass(issueBo);
        if (ObjectUtil.isNull(vo)) {
            throw new ServiceException("这场考试没有配置可用的证书模板，无法补发");
        }
        // 标记为手工补发，跟自动颁发的区分开，便于事后追溯
        CertificateRecord update = new CertificateRecord();
        update.setId(vo.getId());
        update.setIssueType(CertificateRecord.ISSUE_MANUAL);
        update.setRemark(StringUtils.defaultIfBlank(bo.getRemark(), "手工补发"));
        recordMapper.updateById(update);
        vo.setIssueTime(issueBo.getSubmitTime());
        return vo;
    }

    /* ---------------------------------- 私有方法 ---------------------------------- */

    /**
     * 这场考试配的是哪张证书模板
     *
     * @return 模板ID，没配 / 考试不存在时返回 null
     */
    private Long certIdOfExam(Long examId) {
        ExamRef exam = examRefMapper.selectById(examId);
        if (ObjectUtil.isNull(exam) || ObjectUtil.isNull(exam.getCertId()) || exam.getCertId() == 0L) {
            return null;
        }
        return exam.getCertId();
    }

    private String examNameOf(Long examId) {
        if (ObjectUtil.isNull(examId)) {
            return "";
        }
        ExamRef exam = examRefMapper.selectById(examId);
        return ObjectUtil.isNull(exam) ? "" : StringUtils.defaultIfBlank(exam.getExamName(), "");
    }

    private String nickNameOf(Long userId) {
        if (ObjectUtil.isNull(userId)) {
            return "";
        }
        try {
            return StringUtils.defaultIfBlank(remoteUserService.selectNicknameById(userId), "");
        } catch (Exception e) {
            log.warn("查询考生姓名失败 userId={}, {}", userId, e.getMessage());
            return "";
        }
    }

    /**
     * 证书编号：CERT + 日期 + 6 位随机数字
     *
     * <p>唯一索引兜底，撞号就重试；连续撞 5 次直接退成时间戳，总比抛异常打断交卷好。
     */
    private String newCertNo() {
        String day = DateUtil.format(new Date(), "yyyyMMdd");
        for (int i = 0; i < 5; i++) {
            String no = "CERT" + day + RandomUtil.randomNumbers(6);
            Long count = recordMapper.selectCount(
                Wrappers.lambdaQuery(CertificateRecord.class).eq(CertificateRecord::getCertNo, no));
            if (ObjectUtil.isNull(count) || count == 0L) {
                return no;
            }
        }
        return "CERT" + System.currentTimeMillis();
    }

    /**
     * 有效期：永久有效为 null，按天则从颁发日起算
     */
    private Date expireTimeOf(Certificate cert, Date issueTime) {
        if (!Certificate.VALID_DAYS.equals(cert.getValidType())) {
            return null;
        }
        int days = ObjectUtil.defaultIfNull(cert.getValidDays(), 0);
        if (days <= 0) {
            return null;
        }
        return DateUtil.offsetDay(issueTime, days);
    }

    /**
     * 正文占位符替换
     *
     * <p>占位符说明（证书管理页面上有同样的提示）：
     * {nickName} 姓名 / {account} 账号 / {examName} 考试名称 / {score} 得分 /
     * {totalScore} 总分 / {passScore} 及格分 / {certNo} 证书编号 /
     * {issueDate} 颁发日期 / {expireDate} 有效期至
     */
    private String render(String tpl, RemoteCertIssueBo bo, String certNo, Date expireTime) {
        if (StringUtils.isBlank(tpl)) {
            return "";
        }
        Date issueTime = ObjectUtil.defaultIfNull(bo.getSubmitTime(), new Date());
        return tpl
            .replace("{nickName}", StringUtils.defaultIfBlank(bo.getNickName(), ""))
            .replace("{account}", StringUtils.defaultIfBlank(bo.getAccount(), ""))
            .replace("{examName}", StringUtils.defaultIfBlank(bo.getExamName(), ""))
            .replace("{score}", text(bo.getScore()))
            .replace("{totalScore}", text(bo.getTotalScore()))
            .replace("{passScore}", text(bo.getPassScore()))
            .replace("{certNo}", StringUtils.defaultIfBlank(certNo, ""))
            .replace("{issueDate}", DateUtil.formatDate(issueTime))
            .replace("{expireDate}", ObjectUtil.isNull(expireTime) ? "长期有效" : DateUtil.formatDate(expireTime));
    }

    private String text(BigDecimal value) {
        if (ObjectUtil.isNull(value)) {
            return "0";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private BigDecimal nz(BigDecimal value) {
        return ObjectUtil.defaultIfNull(value, BigDecimal.ZERO);
    }

    private RemoteCertVo toRemote(CertificateRecord record) {
        RemoteCertVo vo = MapstructUtils.convert(record, RemoteCertVo.class);
        if (ObjectUtil.isNull(vo)) {
            return null;
        }
        Map<String, String> urls = urlMap(List.of(
            StringUtils.defaultIfBlank(record.getSealOssId(), ""),
            StringUtils.defaultIfBlank(record.getBgOssId(), "")));
        vo.setSealUrl(urls.getOrDefault(record.getSealOssId(), ""));
        vo.setBgUrl(urls.getOrDefault(record.getBgOssId(), ""));
        return vo;
    }

    /** 补上模板名称与图片地址（列表页展示用） */
    private void fillRecordDisplay(List<CertificateRecordVo> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Set<Long> certIds = new HashSet<>();
        List<String> ossIds = new ArrayList<>();
        for (CertificateRecordVo vo : list) {
            if (ObjectUtil.isNotNull(vo.getCertId())) {
                certIds.add(vo.getCertId());
            }
            if (StringUtils.isNotBlank(vo.getSealOssId())) {
                ossIds.add(vo.getSealOssId());
            }
            if (StringUtils.isNotBlank(vo.getBgOssId())) {
                ossIds.add(vo.getBgOssId());
            }
        }
        Map<Long, String> nameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(certIds)) {
            List<Certificate> certs = certificateMapper.selectList(
                Wrappers.lambdaQuery(Certificate.class).in(Certificate::getId, certIds));
            if (CollUtil.isNotEmpty(certs)) {
                certs.forEach(item -> nameMap.put(item.getId(), item.getCertName()));
            }
        }
        Map<String, String> urls = urlMap(ossIds);
        for (CertificateRecordVo vo : list) {
            vo.setCertName(nameMap.getOrDefault(vo.getCertId(), ""));
            vo.setSealUrl(urls.getOrDefault(vo.getSealOssId(), ""));
            vo.setBgUrl(urls.getOrDefault(vo.getBgOssId(), ""));
        }
    }

    /** 补上模板的图片地址 */
    private void fillCertUrl(List<CertificateVo> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<String> ossIds = new ArrayList<>();
        for (CertificateVo vo : list) {
            if (StringUtils.isNotBlank(vo.getSealOssId())) {
                ossIds.add(vo.getSealOssId());
            }
            if (StringUtils.isNotBlank(vo.getBgOssId())) {
                ossIds.add(vo.getBgOssId());
            }
        }
        Map<String, String> urls = urlMap(ossIds);
        list.forEach(vo -> {
            vo.setSealUrl(urls.getOrDefault(vo.getSealOssId(), ""));
            vo.setBgUrl(urls.getOrDefault(vo.getBgOssId(), ""));
        });
    }

    /**
     * 批量换图片地址，一次 Dubbo 调用解决
     *
     * <p>列表页一屏十几条，一条条问文件服务会把页面拖慢；文件服务挂了也只是图片不显示，
     * 不能把整个证书列表打挂，所以异常吞掉返回空 Map。
     */
    private Map<String, String> urlMap(Collection<String> ossIds) {
        Map<String, String> map = new HashMap<>();
        if (CollUtil.isEmpty(ossIds)) {
            return map;
        }
        List<String> ids = ossIds.stream().filter(StringUtils::isNotBlank).distinct().toList();
        if (ids.isEmpty()) {
            return map;
        }
        try {
            String urls = remoteFileService.selectUrlByIds(String.join(",", ids));
            if (StringUtils.isBlank(urls)) {
                return map;
            }
            String[] arr = urls.split(",");
            for (int i = 0; i < ids.size() && i < arr.length; i++) {
                map.put(ids.get(i), arr[i]);
            }
        } catch (Exception e) {
            log.warn("查询证书图片地址失败 {}", e.getMessage());
        }
        return map;
    }

    private void checkNameRepeat(String certName, Long id) {
        if (StringUtils.isBlank(certName)) {
            throw new ServiceException("证书名称不能为空");
        }
        LambdaQueryWrapper<Certificate> lqw = Wrappers.lambdaQuery(Certificate.class)
            .eq(Certificate::getCertName, certName);
        lqw.ne(ObjectUtil.isNotNull(id), Certificate::getId, id);
        if (certificateMapper.selectCount(lqw) > 0) {
            throw new ServiceException("证书名称已存在：" + certName);
        }
    }
}
