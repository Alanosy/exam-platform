package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.mapper.ExamGradeMapper;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.entity.ExamGrade;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.vo.score.GradeScoreVO;
import cn.org.alan.exam.service.IExamGradeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;

/**
 * <p>
 * 服务实现类
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

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;
    @Resource
    private ExamMapper examMapper;

    @Override
    public Result<GradeScoreVO> gradeExamScore(Integer examId, Integer classId) {
        //获取学生的Id
        List<Integer> userIds = userMapper.selectIdsByClassId(classId);
        //获取考试及格分
        Exam exam = examMapper
                .selectOne(new LambdaQueryWrapper<Exam>().select(Exam::getPassedScore).eq(Exam::getId, examId));
        //获取最高分、最低分、平均分、参考人数、及格人数
        GradeScoreVO scoreVO = userExamsScoreMapper.scoreStatistics(userIds, examId, exam.getPassedScore());
        //及格率
        DecimalFormat format = new DecimalFormat("#.00");
        String strPassingPate = format.format((scoreVO.getPassedNum() * 1.0) / (scoreVO.getAttendNum() * 1.0));
        scoreVO.setPassingRate(Double.parseDouble(strPassingPate));
        //获取班级总人数
        Integer totalNum = userMapper
                .selectCount(new LambdaQueryWrapper<User>().eq(User::getGradeId, classId)).intValue();
        //应考人数，缺考人数
        scoreVO.setTotalNum(totalNum);
        scoreVO.setAbsentNum(totalNum - scoreVO.getAttendNum());

        return Result.success(null, scoreVO);
    }
}
