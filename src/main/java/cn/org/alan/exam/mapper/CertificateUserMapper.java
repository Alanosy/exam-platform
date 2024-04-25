package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.CertificateUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface CertificateUserMapper extends BaseMapper<CertificateUser> {

    /**
     * 根据用户id列表批量删除用户的证书
     * @param userIds 用户id列表
     * @return 影响数据库记录数
     */
    Integer deleteByUserIds(List<Integer> userIds);
}
