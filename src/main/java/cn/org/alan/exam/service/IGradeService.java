package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.form.GradeForm;
import cn.org.alan.exam.model.form.count.ClassCountResult;
import cn.org.alan.exam.model.vo.GradeVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 班级服务类
 *
 * @author WeiJin
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

    /**
     * 获取所有班级列表
     * @return
     */
    Result<List<GradeVO>> getAllGrade();


    /**
     * 各班人数统计
     * @return
     */
    //声明统计role_id为1的各班人数的业务逻辑
    List<ClassCountResult> countStudentsByRoleId(int roleId);



    /**
     * 统计所有班级、试卷、试题数量
     * @return
     */
    Result<ClassCountResult> getAllCounts();
}



