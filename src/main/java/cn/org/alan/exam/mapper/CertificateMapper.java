package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Certificate;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.form.CertificateForm;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    /**
     * 根据ID查询证书
     *
     * @param id 证书ID
     * @return 证书实体
     */
    Certificate selectById(@Param("id") String id);

    /**
     * 新增证书
     *
     * @param certificate 证书实体
     * @return 影响行数
     */
    int insertCertificate(Certificate certificate);
    /**
     * 根据条件分页查询证书
     *
     * @param page         分页对象
     * @param certificateName 证书名
     * @param certificationUnit 认证单位
     * @param image      证书背景图片
     * @return 分页结果
     */
    IPage<Certificate> pagingCertificate(@Param("page") Page<Certificate> page,
                                         @Param("certificateName") String certificateName,
                                         @Param("certificationUnit") String certificationUnit,
                                         @Param("image") String image);


    /**
     * 更新证书信息
     *
     * @param certificate 待更新的证书实体
     * @return 更新影响行数
     */
    int updateById(@Param("certificate") Certificate certificate);
//    学生端获取证书
//    @Select("select * from t_certificate where id = #{id}")  //括号里写sql语句
//    List<Certificate> select(Integer id);

    /**
     * 删除证书
     *
     * @param id 证书ID
     * @return 删除影响行数
     */
    //删除证书
//    int deleteCertificate(List<Integer> Id);
    int deleteById(@Param("id") String id);

}
