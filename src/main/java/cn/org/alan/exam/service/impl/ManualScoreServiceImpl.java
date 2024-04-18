package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.ManualScoreMapper;
import cn.org.alan.exam.model.entity.ManualScore;
import cn.org.alan.exam.model.form.answer.AnswerUpdate;
import cn.org.alan.exam.model.vo.answer.AnswerExamPageVO;
import cn.org.alan.exam.model.vo.answer.AnswerPageVO;
import cn.org.alan.exam.service.IManualScoreService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class ManualScoreServiceImpl extends ServiceImpl<ManualScoreMapper, ManualScore> implements IManualScoreService {


    @Override
    public Result getDetail(Integer userId, Integer examId) {
        return null;
    }

    @Override
    public Result<IPage<AnswerPageVO>> getPaging(Integer pageNum, Integer pageSize) {
        return null;
    }

    @Override
    public Result Correct(AnswerUpdate answerUpdate) {
        return null;
    }

    @Override
    public Result<IPage<AnswerExamPageVO>> examPage() {

        // 查找自己创建的考试
        // SELECT id,title FROM t_exam WHERE user_id = 1

        // 查询考试班级人数（总人数）
        // SELECT COUNT(*) FROM t_grade WHERE id in (SELECT grade_id FROM t_exam_grade WHERE exam_id= 1)

        // 查询已考人数（待阅卷）
        // SELECT COUNT(*) FROM t_user_exams_score WHERE state=1 and exam_id= 1

        // 查询已改试卷数（已阅卷）
        // SELECT COUNT(*) FROM t_user_exams_score WHERE whether_mark=1

        return null;
    }
}
