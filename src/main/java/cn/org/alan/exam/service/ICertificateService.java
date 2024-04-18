package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Certificate;
import cn.org.alan.exam.model.form.CertificateForm;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author JinXi
 * @since 2024-03-21
 */

public interface ICertificateService extends IService<Certificate> {
    //查询在用的证书列表

    /**
     * 添加证书
     * @param certificateForm 参数
     * @return 返回
     */
    //新增证书的addCertificate方法（在CertificateController中调用）
    Result<String> addCertificate(CertificateForm certificateForm);


    /**
     * 分页查询证书
     * @param pageNum  页码
     * @param pageSize 每页记录数
     * @param certificateName    证书名
     * @param certificationUnit    认证单位
     * @param image    证书背景图片
     * @return 返回结果响应
     */
    //获取后台分页证书 实现类
    Result<IPage<Certificate>> pagingCertificate(Integer pageNum, Integer pageSize, String certificateName, String certificationUnit, String image);

    //学生端获取证书
    List<Certificate> list(Integer id);


    /**
     * 修改公告
     * @param id
     * @return
     */
    //修改证书

    //修改证书
    Result<String> updateCertificate(String id, CertificateForm certificateForm);

    /**
     * 删除公告
     * @param id
     * @return
     */
    Result<String> deleteCertificate(String id);


}
