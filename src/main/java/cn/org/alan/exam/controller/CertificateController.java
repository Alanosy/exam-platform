package cn.org.alan.exam.controller;


import cn.org.alan.exam.common.group.CertificateGroup;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Certificate;
import cn.org.alan.exam.model.form.CertificateForm;
import cn.org.alan.exam.service.ICertificateService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 *  证书管理
 *
 * @author zsx
 * @since 2024-04-1
 */
@RestController
@RequestMapping("/api/certificate")
public class CertificateController {

    @Resource
    private ICertificateService iCertificateService;

    /**
     * 添加证书，只有教师和管理员可以添加证书
     * @param certificateForm 添加证书的前端参数
     * @return 返回响应结果
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> addCertificate(@RequestBody @Validated(CertificateGroup.CertificateInsertGroup.class)
                                             CertificateForm certificateForm) {
        //从token获取用户id，放入创建人id属性
        return iCertificateService.addCertificate(certificateForm);
    }

    /**
     * 分页查询证书
     * @param pageNum           页码
     * @param pageSize          每页记录数
     * @param certificateName   证书名
     * @param certificationUnit 认证单位
     * @param image             证书背景图片
     * @return 响应结果
     */
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<Certificate>> pagingCertificate(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                        @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                        @RequestParam(value = "certificateName", required = false) String certificateName,
                                                        @RequestParam(value = "certificationUnit", required = false) String certificationUnit,
                                                        @RequestParam(value = "image", required = false) String image) {
        return iCertificateService.pagingCertificate(pageNum, pageSize, certificateName, certificationUnit, image);
    }

    /**
     * 修改证书
     * @param id  id
     * @param certificateForm 修改证书的前端参数
     * @return 返回响应结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> updateCertificate(@PathVariable("id") Integer id,
                                            @RequestBody CertificateForm certificateForm) {
        certificateForm.setId(id);
        return iCertificateService.updateCertificate(certificateForm);
    }

    /**
     * 删除证书
     * @param id id
     * @return 返回响应结果
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> deleteCertificate(@PathVariable("id") Integer id) {
        return iCertificateService.deleteCertificate(id);
    }
}
