package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.mapper.ExamGradeMapper;
import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.entity.ExamGrade;
import cn.org.alan.exam.service.IExamGradeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
public class ExamGradeServiceImpl extends ServiceImpl<ExamGradeMapper, ExamGrade> implements IExamGradeService {

    //各班试卷信息   统计实现ExamGradeService接口，在ExamGradeServiceImpl类中调用Mapper方法：
    @Autowired
    private ExamGradeMapper examGradeMapper;

    /**
     * 各班试卷统计信息
     * @return
     */
    @Override
    public List<Exam> getExamGradeCount(int roleId) {
        return examGradeMapper.ExamGradeCount(roleId);
    }

}
