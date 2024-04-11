package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Certificate;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.form.CertificateForm;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    //新增证书
    Integer insertCertificate(Certificate certificate);

    /**
     * 分页查询证书
     * @param pageNum  页码
     * @param pageSize 每页记录数
     * @param certificateName    证书名
     * @param certificationUnit    认证单位
     * @param image    证书背景图片
     */
    //分页查询证书
    IPage<Certificate> pagingCertificate(@Param("pageNum") IPage<Certificate> pageNum,
                                         @Param("pageSize") IPage<Certificate> pageSize,
                                         @Param("certificateName") String certificateName,
                                         @Param("certificationUnit") String certificationUnit,
                                         @Param("image") String image);


    IPage<Certificate> pagingCertificate(IPage<Certificate> page, String certificateName, Integer userId);

    //学生端获取证书
    @Select("select * from t_certificate where id = #{id}")  //括号里写sql语句
    List<Certificate> select(Integer id);


    //删除证书
    int deleteCertificate(List<Integer> Id);

}
