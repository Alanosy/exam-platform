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
 * @author Jinci
 * @since 2024-03-21
 */
@Service
public  class CertificateServiceImpl extends ServiceImpl<CertificateMapper, Certificate> implements ICertificateService {
    //添加属性certificateMapper
    @Resource
    private CertificateMapper certificateMapper;

    
    //新增证书
    @Override
    public Result<String> addCertificate(CertificateForm certificate) {
        certificateMapper.insertCertificate(certificateMapper.selectById(1));
        return Result.success("新增证书成功");
    }

    //查询证书分页信息的方法  实现类
    @Override
    public Result<IPage<Certificate>> pagingCertificate(Integer pageNum, Integer pageSize, String certificateName,String certificationNuit,String image) {

        IPage<Certificate> page = new Page<>(pageNum,pageSize);
        //按角色查看证书
        if ("role_teacher".equals(SecurityUtil.getRole())) {
            //教师只查询自己的证书
            page = certificateMapper.pagingCertificate(page, certificateName, SecurityUtil.getUserId());
        } else {
            //管理员可以获取所有证书
            page = certificateMapper.pagingCertificate(page, certificateName, 0);
        }

        return Result.success(null, page);
    }


    //学生端获取证书
    public List<Certificate> list(Integer id) {
        QueryWrapper<Certificate> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(Certificate::getId,id);  //使用lambda表达式设置查询条件  （等于id）

        return this.list(wrapper); //执行查询并返回符合条件的所有Certificate对象列表
    }


    //修改证书
    @Override
    public Result<String> updateCertificate(String id, CertificateForm certificateForm) {
        LambdaUpdateWrapper<Certificate> certificateLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        certificateLambdaUpdateWrapper
                .set(Certificate::getCertificateName, certificateForm.getCertificateName())
                .eq(Certificate::getId, id);
        int rowsAffected = certificateMapper.update(certificateLambdaUpdateWrapper);
        if (rowsAffected == 0) {
            return Result.failed("修改失败");
        }
        return Result.success("修改成功");
    }

    //删除证书
    @Override
    public Result<String> deleteCertificate(String id) {
        List<Integer> Id = Arrays.stream(id.split(","))
                .map(Integer::parseInt)
                .toList();
        int rowsAffected = certificateMapper.deleteCertificate(Id);
        if (rowsAffected == 0) {
            return Result.failed("删除失败");
        }
        return Result.success("删除成功");
    }

}
