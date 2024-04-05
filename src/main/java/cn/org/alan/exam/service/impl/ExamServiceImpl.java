package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.ExamMapper;
import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.form.ExamForm;
import cn.org.alan.exam.model.vo.*;
import cn.org.alan.exam.service.IExamService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements IExamService {

    @Override
    public Result<String> createExam(ExamForm examForm) {









        return null;
    }

    @Override
    public Result<String> updateExam(ExamForm examForm) {
        return null;
    }

    @Override
    public Result<String> deleteExam(String ids) {
        return null;
    }

    @Override
    public Result<IPage<ExamVO>> getPagingExam(Integer pageNum, Integer pageSize, Integer title) {
        return null;
    }

    @Override
    public Result<ExamDetailRespVO> getQuestionList(String id) {
        return null;
    }

    @Override
    public Result<PaperQuDetailVO> getQuestionSingle(String examId, String questionId) {
        return null;
    }

    @Override
    public Result<EXamQuCollectVO> getCollect(String id) {
        return null;
    }

    @Override
    public Result<ExamDetailVO> getDetail(String id) {
        return null;
    }

    @Override
    public Result<String> addCheat() {
        return null;
    }

    @Override
    public Result<ExamFillVO> addAnswer() {

        // 未作答
    //     if(CollectionUtils.isEmpty(reqDTO.getAnswers())
    //             && StringUtils.isBlank(reqDTO.getAnswer())){
    //         return;
    //     }
    //
    //     //查找答案列表
    //     List<PaperQuAnswer> list = paperQuAnswerService.listForFill(reqDTO.getPaperId(), reqDTO.getQuId());
    //
    //     //是否正确
    //     boolean right = true;
    //
    //     //更新正确答案
    //     for (PaperQuAnswer item : list) {
    //
    //         if (reqDTO.getAnswers().contains(item.getId())) {
    //             item.setChecked(true);
    //         } else {
    //             item.setChecked(false);
    //         }
    //
    //         //有一个对不上就是错的
    //         if (item.getIsRight()!=null && !item.getIsRight().equals(item.getChecked())) {
    //             right = false;
    //         }
    //         paperQuAnswerService.updateById(item);
    //     }
    //
    //     //修改为已回答
    //     PaperQu qu = new PaperQu();
    //     qu.setQuId(reqDTO.getQuId());
    //     qu.setPaperId(reqDTO.getPaperId());
    //     qu.setIsRight(right);
    //     qu.setAnswer(reqDTO.getAnswer());
    //     qu.setAnswered(true);
    //
    //     paperQuService.updateByKey(qu);
        return null;
    }

    @Override
    public Result<ExamVO> getGradeExamList(String id) {
        return null;
    }
}
