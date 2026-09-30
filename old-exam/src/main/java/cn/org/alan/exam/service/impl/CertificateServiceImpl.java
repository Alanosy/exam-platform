package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.CertificateConverter;
import cn.org.alan.exam.mapper.CertificateMapper;
import cn.org.alan.exam.model.entity.Certificate;
import cn.org.alan.exam.model.form.cretificate.CertificateForm;
import cn.org.alan.exam.model.vo.certificate.MyCertificateVO;
import cn.org.alan.exam.service.ICertificateService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 证书服务实现类
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

    @Override
    @Transactional
    public Result<String> addCertificate(CertificateForm certificateForm) {
        Certificate certificate = certificateConverter.fromToEntity(certificateForm);
        // 自动生成时间
        int insertRows = certificateMapper.insert(certificate);
        if (insertRows > 0) {
            return Result.success("添加证书成功");
        }
        throw new ServiceRuntimeException("添加证书失败");
    }

    @Override
    public Result<IPage<Certificate>> pagingCertificate(Integer pageNum, Integer pageSize, String certificateName, String certificationUnit) {
        IPage<Certificate> page = new Page<>(pageNum, pageSize);
        Integer userId = SecurityUtil.getUserId();
        page = certificateMapper.selectCertificatePage(page, userId, certificateName, certificationUnit);
        return Result.success("查询成功", page);
    }


    @Override
    @Transactional
    public Result<String> updateCertificate(CertificateForm certificateForm) {
        Certificate certificate = certificateConverter.fromToEntity(certificateForm);
        // 根据证书ID更新证书
        Integer affectedRows = certificateMapper.updateById(certificate);
        if (affectedRows > 0) {
            return Result.success("修改证书成功");
        }
        throw new ServiceRuntimeException("修改证书失败");

    }

    @Override
    @Transactional
    public Result<String> deleteCertificate(Integer id) {
        int affectedRows = certificateMapper.deleteById(id);
        if (affectedRows > 0) {
            return Result.success("删除证书成功");
        }
        throw new ServiceRuntimeException("删除证书失败");

    }

    @Override
    public Result<IPage<MyCertificateVO>> getMyCertificatePaging(Integer pageNum, Integer pageSize, String examName) {
        Page<MyCertificateVO> myCertificateVOPage = new Page<>();
        myCertificateVOPage = certificateMapper.selectMyCertificate(myCertificateVOPage, pageNum, pageSize, SecurityUtil.getUserId(), examName);
        return Result.success("查询自己获得的证书成功", myCertificateVOPage);
    }

}
