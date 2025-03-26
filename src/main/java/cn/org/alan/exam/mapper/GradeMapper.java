package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.vo.grade.GradeVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 班级表 Mapper 接口
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface GradeMapper extends BaseMapper<Grade> {

    /**
     * 分页查找班级
     *
     * @param page        分页对象
     * @param userId      用户ID
     * @param gradeName   班级名称
     * @param roleCode    角色代码
     * @param gradeIdList 班级ID列表
     * @return 分页结果
     */
    Page<GradeVO> selectGradePage(Page<GradeVO> page, Integer userId, String gradeName, Integer roleCode, List<Integer> gradeIdList);

    /**
     * 获得所有班级
     *
     * @param userId      用户ID
     * @param roleCode    觉得代码
     * @param gradeIdList 班级ID列表
     * @return 结果集
     */
    List<GradeVO> getAllGrade(Integer userId, Integer roleCode, List<Integer> gradeIdList);

    /**
     * 根据班级代码获取班级对象
     *
     * @param code 班级代码
     * @return 班级对象
     */
    Grade getGradeByCode(String code);

}
