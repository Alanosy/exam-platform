package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.form.GradeForm;
import cn.org.alan.exam.model.vo.GradeVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 班级服务类
 *
 * @author Alan
 * @since 2024-03-21
 */
public interface IGradeService extends IService<Grade> {
    /**
     * 添加班级
     *
     * @param userId
     * @param gradeForm
     * @return
     */
    Result<String> addGrade(Integer userId, GradeForm gradeForm);

    /**
     * 修改班级
     *
     * @param id
     * @param gradeForm
     * @return
     */
    Result<String> updateGrade(String id, GradeForm gradeForm);

    /**
     * 删除班级
     *
     * @param id
     */
    Result<String> deleteGrade(Integer id);

    /**
     * 分页查询班级
     *
     * @param userId
     * @param pageNum
     * @param pageSize
     * @param gradeName
     * @return
     */
    Result<IPage<GradeVO>> getPaging(Integer userId, Integer pageNum, Integer pageSize, String gradeName);

    /**
     * 批量移除班级
     *
     * @param ids
     * @return
     */
    Result<String> removeUserGrade(String ids);

    /**
     * 查询所有班级
     *
     * @return
     */
    Result<List<GradeVO>> getGradeAll(Integer userId);

    /**
     * 用户加入班级
     *
     * @param userId
     * @param code
     * @return
     */
    Result<String> userAddGrade(Integer userId, String code);


}
