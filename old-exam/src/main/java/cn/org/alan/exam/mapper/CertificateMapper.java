package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Certificate;
import cn.org.alan.exam.model.vo.certificate.MyCertificateVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 证书 Mapper 接口
 *
 * @author JinXin
 * @since 2024-03-21
 */
public interface CertificateMapper extends BaseMapper<Certificate> {

    /**
     * 分页查找学生自己获得的证书
     *
     * @param myCertificateVOPage 分页page对象
     * @param pageNum             页码
     * @param pageSize            每页大小
     * @param userId              用户ID
     * @param examName            试卷名称
     * @return 分页结果
     */
    Page<MyCertificateVO> selectMyCertificate(Page<MyCertificateVO> myCertificateVOPage, Integer pageNum, Integer pageSize, Integer userId, String examName);

    /**
     * 教师和管理员分页查询证书
     *
     * @param page              分页对象
     * @param userId            用户ID
     * @param certificateName   证明名称
     * @param certificationUnit 证书单位名称
     * @return 证书相关Page对象
     */
    IPage<Certificate> selectCertificatePage(IPage<Certificate> page, Integer userId, String certificateName, String certificationUnit);

}
