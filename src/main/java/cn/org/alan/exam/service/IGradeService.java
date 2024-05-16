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
     * @param gradeForm
     * @return
     */
    Result<String> addGrade(GradeForm gradeForm);

    /**
     * 修改班级
     *
     * @param id
     * @param gradeForm
     * @return
     */
    Result<String> updateGrade(Integer id, GradeForm gradeForm);

    /**
     * 删除班级
     *
     * @param id
     * @return
     */
    Result<String> deleteGrade(Integer id);

    /**
     * 分页查找班级
     *
     * @param pageNum
     * @param pageSize
     * @param gradeName
     * @return
     */
    Result<IPage<GradeVO>> getPaging(Integer pageNum, Integer pageSize, String gradeName);

    /**
     * 移除班级
     *
     * @param ids
     * @return
     */
    Result<String> removeUserGrade(String ids);
    // 获取所有班级列表
    Result<List<GradeVO>> getAllGrade();
}



