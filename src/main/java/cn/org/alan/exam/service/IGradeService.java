package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.form.GradeForm;
import cn.org.alan.exam.model.vo.GradeVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IGradeService extends IService<Grade> {


    Result<String> addGrade( GradeForm gradeForm);

    Result<String> updateGrade(Integer id, GradeForm gradeForm);

    Result<String> deleteGrade(Integer id);

    Result<IPage<GradeVO>> getPaging( Integer pageNum, Integer pageSize, String gradeName);

    Result<String> removeUserGrade(String ids);
}
