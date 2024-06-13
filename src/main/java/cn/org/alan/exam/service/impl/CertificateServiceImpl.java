package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.CertificateConverter;
import cn.org.alan.exam.mapper.CertificateMapper;
import cn.org.alan.exam.model.entity.Certificate;
import cn.org.alan.exam.model.form.CertificateForm;
import cn.org.alan.exam.model.vo.GradeVO;
import cn.org.alan.exam.model.vo.NoticeVO;
import cn.org.alan.exam.model.vo.certificate.MyCertificateVO;
import cn.org.alan.exam.service.ICertificateService;
import cn.org.alan.exam.util.CacheClient;
import cn.org.alan.exam.util.DateTimeUtil;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Jinxin
 * @since 2024-03-21
 */
@Service
public class CertificateServiceImpl extends ServiceImpl<CertificateMapper, Certificate> implements ICertificateService {

    @Resource
    private CertificateMapper certificateMapper;
    @Resource
    private CertificateConverter certificateConverter;
    @Resource
    private CacheClient cacheClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    //新增证书
    @Override
    @Transactional
    public Result<String> addCertificate(CertificateForm certificateForm) {

        Certificate certificate = certificateConverter.fromToEntity(certificateForm);
        //自动生成时间
        int insertRows = certificateMapper.insert(certificate);

        if (insertRows > 0) {
            if (certificate.getId() != null) { // 确保ID有效
                // 如果是更新操作，先从缓存中移除旧数据，然后重新放入最新的数据
                stringRedisTemplate.delete("cache:certificate:pagingCertificate:"+certificate.getId().toString()); // 删除旧缓存
                Map<Integer, Certificate> map = Map.of(certificate.getId(), certificate);
                cacheClient.batchPut("cache:certificate:pagingCertificate:",map,10L,TimeUnit.MINUTES); // 存储新数据
            }
            return Result.success("添加证书成功");
        } else {
            return Result.failed("添加证书失败");
        }
    }

    //查询证书分页信息的方法  实现类
    @Override
    public Result<IPage<Certificate>> pagingCertificate(Integer pageNum, Integer pageSize, String certificateName,
                                                        String certificationUnit) {
        // 查询满足条件的总记录数
        int total = certificateMapper.countByCondition(SecurityUtil.getUserId(), certificateName,certificationUnit); // 假设gradeMapper中实现了根据条件计数的方法
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询分页ID列表
        List<Integer> certificateIds = certificateMapper.selectCertificateIdsPage(SecurityUtil.getUserId(),  certificateName,certificationUnit, offset, pageSize);

        // 批量从缓存中获取GradeVO对象
        Map<Integer, Certificate> cachedGradesMap = cacheClient.batchGet("cache:certificate:pagingCertificate:",certificateIds, Certificate.class);

        // 确定未命中的ID列表
        List<Integer> missIds = new ArrayList<>();
        for (Integer id : certificateIds) {
            if (!cachedGradesMap.containsKey(id)) {
                missIds.add(id);
            }
        }

        // 如果有未命中的ID，从数据库批量查询并更新缓存
        if (!missIds.isEmpty()) {
            List<Certificate> missedGrades = certificateMapper.batchSelectByIds(missIds);
            // 假设GradeVO的ID为getId()，使用Collectors.toMap转换
            Map<Integer, Certificate> missedGradesMap = missedGrades.stream()
                    .collect(Collectors.toMap(Certificate::getId, Function.identity()));
            // 更新缓存
            cacheClient.batchPut("cache:certificate:pagingCertificate:",missedGradesMap,10L, TimeUnit.MINUTES);
            // 合并缓存结果
            cachedGradesMap.putAll(missedGradesMap);
        }

        // 根据ID列表从缓存中获取完整的GradeVO对象列表
        List<Certificate> finalResult = new ArrayList<>(certificateIds.size());
        for (Integer id : certificateIds) {
            finalResult.add(cachedGradesMap.get(id));
        }

        // 构建并返回IPage对象
        IPage<Certificate> resultPage = new Page<>(pageNum, pageSize, Long.valueOf(total));
        resultPage.setRecords(finalResult);

        return Result.success("查询成功", resultPage);
    }


    @Override
    @Transactional
    public Result<String> updateCertificate(CertificateForm certificateForm) {
        Certificate certificate = certificateConverter.fromToEntity(certificateForm);

        // 调用mapper方法更新证书
        int affectedRows = certificateMapper.updateById(certificate);

        if (affectedRows > 0) {
            if (certificate.getId() != null) { // 确保ID有效
                // 如果是更新操作，先从缓存中移除旧数据，然后重新放入最新的数据
                stringRedisTemplate.delete("cache:certificate:pagingCertificate:"+certificate.getId().toString()); // 删除旧缓存
                Map<Integer, Certificate> map = Map.of(certificate.getId(), certificate);
                cacheClient.batchPut("cache:certificate:pagingCertificate:",map,10L,TimeUnit.MINUTES); // 存储新数据
            }
            return Result.success("修改证书成功");
        } else {
            return Result.failed("修改证书失败");
        }
    }

    @Override
    @Transactional
    public Result<String> deleteCertificate(Integer id) {
        LambdaUpdateWrapper<Certificate> certificateLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        certificateLambdaUpdateWrapper.eq(Certificate::getId,id)
                .set(Certificate::getIsDeleted,1);
        int affectedRows = certificateMapper.update(certificateLambdaUpdateWrapper);

        if (affectedRows > 0) {
            stringRedisTemplate.delete("cache:certificate:pagingCertificate:"+id);
            return Result.success("删除证书成功");
        } else {
            return Result.failed("删除证书失败");
        }
    }

    @Override
    public Result<IPage<MyCertificateVO>> getMyCertificatePaging(Integer pageNum, Integer pageSize,String examName) {
        Page<MyCertificateVO> myCertificateVOPage = new Page<>();
        myCertificateVOPage = certificateMapper.selectMyCertificate(myCertificateVOPage,pageNum,pageSize, SecurityUtil.getUserId(),examName);
        return Result.success("查询成功",myCertificateVOPage);
    }

}
