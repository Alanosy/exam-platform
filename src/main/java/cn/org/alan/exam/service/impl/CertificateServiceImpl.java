package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.CertificateConverter;
import cn.org.alan.exam.mapper.CertificateMapper;
import cn.org.alan.exam.model.entity.Certificate;
import cn.org.alan.exam.model.form.CertificateForm;
import cn.org.alan.exam.service.ICertificateService;
import cn.org.alan.exam.util.DateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


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

    //新增证书
    @Override
    public Result<String> addCertificate(CertificateForm certificateForm) {

        Certificate certificate = certificateConverter.fromToEntity(certificateForm);
        //自动生成时间
        int insertRows = certificateMapper.insert(certificate);

        if (insertRows > 0) {
            return Result.success("添加证书成功");
        } else {
            return Result.failed("添加证书失败");
        }
    }

    //查询证书分页信息的方法  实现类
    @Override
    public Result<IPage<Certificate>> pagingCertificate(Integer pageNum, Integer pageSize, String certificateName,
                                                        String certificationUnit, String image) {

        Page<Certificate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<Certificate>()
                .like(StringUtils.isNotBlank(certificateName), Certificate::getCertificateName, certificateName)
                .like(StringUtils.isNotBlank(certificationUnit), Certificate::getCertificationNuit, certificationUnit)
                .eq(StringUtils.isNotBlank(image), Certificate::getImage, image);

        Page<Certificate> certificatePage = certificateMapper.selectPage(page, wrapper);

        return Result.success("查询成功", certificatePage);


    }


    @Override
    @Transactional
    public Result<String> updateCertificate(CertificateForm certificateForm) {


        Certificate certificate = certificateConverter.fromToEntity(certificateForm);

        // 调用mapper方法更新证书
        int affectedRows = certificateMapper.updateById(certificate);

        if (affectedRows > 0) {
            return Result.success("修改证书成功");
        } else {
            return Result.failed("修改证书失败");
        }
    }


//    //修改证书
//    @Override
//    @Transactional
//    public Result<String> updateCertificate(Integer id, CertificateForm certificateForm) {
//        // 验证ID和表单非空
//        if (id == null || certificateForm == null) {
//            return Result.failed("参数错误");
//        }
//
//        // 根据ID查询现有证书
//        Certificate existingCertificate = certificateMapper.selectById(id);
//         if (existingCertificate == null) {
//            return Result.failed("待更新的证书不存在");
//        }
//
//        //更新必要的字段
//        existingCertificate.setCertificateName(certificateForm.getCertificateName());
//        existingCertificate.setCertificationNuit(certificateForm.getCertificationNuit());
//
//
//        // 调用mapper方法更新证书
//        int affectedRows = certificateMapper.updateById(existingCertificate);
//
//        if (affectedRows > 0) {
//            return Result.success("修改证书成功");
//        } else {
//            return Result.failed("修改证书失败");
//        }
//    }

    @Override
    public Result<String> deleteCertificate(Integer id) {
        int affectedRows = certificateMapper.deleteById(id);


        if (affectedRows > 0) {
            return Result.success("删除证书成功");
        } else {
            return Result.failed("删除证书失败");
        }
    }

}
