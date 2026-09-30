package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.form.grade.GradeForm;
import cn.org.alan.exam.model.vo.grade.GradeVO;
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
     * @param gradeForm
     * @return
     */
    Result<String> addGrade(GradeForm gradeForm);

    /**
     * 修改班级
     *
     * @param id        班级ID
     * @param gradeForm
     * @return
     */
    Result<String> updateGrade(Integer id, GradeForm gradeForm);

    /**
     * 删除班级
     *
     * @param id 班级ID
     * @return
     */
    Result<String> deleteGrade(Integer id);

    /**
     * 分页查找班级
     *
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param gradeName 班级名称
     * @return
     */
    Result<IPage<GradeVO>> getPaging(Integer pageNum, Integer pageSize, String gradeName);

    /**
     * 移除班级
     *
     * @param ids 班级代码
     * @return
     */
    Result<String> removeUserGrade(String ids);

    /**
     * 获取所有班级列表
     *
     * @return
     */
    Result<List<GradeVO>> getAllGrade();

    /**
     * 老师加入班级
     *
     * @param code 班级代码
     * @return
     */
    Result teacherJoinClass(String code);

    /**
     * 老师退出班级
     *
     * @param gradeId 班级ID
     * @return
     */
    Result teacherExitClass(String gradeId);

    /**
     * 学生退出班级
     *
     * @return
     */
    Result userExitGrade();
}



