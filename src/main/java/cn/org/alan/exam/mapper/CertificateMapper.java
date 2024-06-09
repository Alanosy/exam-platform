package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Certificate;
import cn.org.alan.exam.model.form.CertificateForm;
import cn.org.alan.exam.model.vo.certificate.MyCertificateVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author JinXin
 * @since 2024-03-21
 */
@Repository
public interface CertificateMapper extends BaseMapper<Certificate> {

    Page<MyCertificateVO> selectMyCertificate(Page<MyCertificateVO> myCertificateVOPage, Integer pageNum, Integer pageSize, Integer userId,String examName);
}
