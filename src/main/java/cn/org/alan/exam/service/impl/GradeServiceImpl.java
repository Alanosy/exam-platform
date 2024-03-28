package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.converter.GradeConverter;
import cn.org.alan.exam.mapper.GradeMapper;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.GradeForm;
import cn.org.alan.exam.service.IGradeService;
import cn.org.alan.exam.util.ClassTokenGenerator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
public class GradeServiceImpl extends ServiceImpl<GradeMapper, Grade> implements IGradeService {
    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private GradeConverter gradeConverter;


    @Override
    public void addGrade(GradeForm gradeForm) {
        gradeForm.setCode(ClassTokenGenerator.generateClassToken(18));
        Grade grade = gradeConverter.formEntity(gradeForm);
        gradeMapper.insert(grade);
    }

    @Override
    public void updateGrade(Integer id, GradeForm gradeForm) {
        UpdateWrapper<Grade> gradeUpdateWrapper = new UpdateWrapper<>();
        gradeUpdateWrapper
                .set("grade_name",gradeForm.getGradeName())
                .eq("id",id);
        gradeMapper.update(gradeUpdateWrapper);
    }

    @Override
    public void deleteGrade(Integer id) {
        gradeMapper.deleteById(id);
    }

    @Override
    public void getPaging(Integer pageNum, Integer pageSize, String gradeName) {

        Page<Grade> page = new Page<>(pageNum,pageSize);
        QueryWrapper<Grade> gradeQueryWrapper = new QueryWrapper<>();
        gradeQueryWrapper.eq("grade_name",gradeName);
        gradeMapper.selectPage(page,gradeQueryWrapper);

    }
}
