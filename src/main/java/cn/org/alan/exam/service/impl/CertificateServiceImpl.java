package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.CertificateMapper;
import cn.org.alan.exam.model.entity.Certificate;
import cn.org.alan.exam.model.form.CertificateForm;
import cn.org.alan.exam.service.ICertificateService;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Jinxin
 * @since 2024-03-21
 */
@Service
public class CertificateServiceImpl extends ServiceImpl<CertificateMapper, Certificate> implements ICertificateService {
    @Resource
    private CertificateMapper certificateMapper;

    //新增证书
    @Override
    public Result<String> addCertificate(CertificateForm certificate) {

        int insertRows = certificateMapper.insertCertificate(certificateMapper.selectById(1));

        if (insertRows > 0) {
            return Result.success("添加证书成功");
        } else {
            return Result.failed("添加证书失败");
        }
    }

    //查询证书分页信息的方法  实现类
    @Override
    public Result<IPage<Certificate>> pagingCertificate(Integer pageNum, Integer pageSize, String certificateName, String certificationUnit, String image) {
        Page<Certificate> page = new Page<>(pageNum, pageSize);
        IPage<Certificate> certificatePage = certificateMapper.pagingCertificate(page, certificateName, certificationUnit, image);

        return Result.success(null,certificatePage);
    }

    @Override
    public Result<String> updateCertificate(String id, CertificateForm certificateForm) {
        // 根据ID查询现有证书
        Certificate existingCertificate = certificateMapper.selectById(id);

        if (existingCertificate == null) {
            return Result.failed("待更新的证书不存在");
        }


        // 调用mapper方法更新证书
        int affectedRows = certificateMapper.updateById(existingCertificate);

        if (affectedRows > 0) {
            return Result.success("修改证书成功");
        } else {
            return Result.failed("修改证书失败");
        }
    }

    @Override
    public Result<String> deleteCertificate(String id) {
        int affectedRows = certificateMapper.deleteById(id);

        if (affectedRows > 0) {
            return Result.success("删除证书成功");
        } else {
            return Result.failed("删除证书失败");
        }
    }

}
