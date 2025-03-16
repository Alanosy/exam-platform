package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.count.ClassCountResult;
import cn.org.alan.exam.model.vo.UserVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Repository;


import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Repository
public interface UserMapper extends BaseMapper<User> {

    // List<User> getText(Integer id);
    Integer removeUserGrade(List<Integer> userIds);

    /**
     * 批量添加用户
     *
     * @param list
     * @return
     */
    Integer insertBatchUser(List<User> list);

    /**
     * 获取用户信息
     *
     * @param userId 用户id
     * @return 响应
     */
    UserVO info(Integer userId);

    /**
     * 根据班级Id获取该班级学生Id
     *
     * @param classId 班级Id
     * @return 结果集
     */
    List<Integer> selectIdsByClassId(Integer classId);

    /**
     * 分页获取用户信息
     *
     * @param page     分页信息
     * @param gradeId  班级Id
     * @param realName 真实姓名
     * @param roleId   角色Id
     * @return 查询结果集
     */
    IPage<UserVO> pagingUser(IPage<UserVO> page, Integer gradeId, String realName,Integer userId, Integer roleId);

    /**
     * 移除班级
     * @param gradeIds 班级id列表
     * @return 影响数据库记录数
     */
    Integer removeGradeIdByGradeIds(List<Integer> gradeIds);

    List<ClassCountResult> countAndGroupByGradeAndRoleId(Integer roleId);

    /**
     * 删除用户
     * @param userIds
     * @return
     */
    Integer deleteByUserIds(List<Integer> userIds);

    /**
     * 用户退出班级
     * @param gradeId
     * @param userId
     * @return
     */
    Integer userExitGrade(Integer gradeId, Integer userId);

    /**
     * 获得管理员ID列表
     * @return
     */
    List<Integer> getAdminList();
}
