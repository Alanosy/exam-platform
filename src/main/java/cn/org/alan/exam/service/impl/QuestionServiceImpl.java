package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.QuestionConverter;
import cn.org.alan.exam.mapper.OptionMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.model.entity.Option;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.form.QuestionFrom;
import cn.org.alan.exam.model.vo.QuestionVO;
import cn.org.alan.exam.service.IQuestionService;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {

    @Resource
    private QuestionConverter questionConverter;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private OptionMapper optionMapper;

    @Override
    @Transactional
    public Result<String> addSingleQuestion(QuestionFrom questionFrom) {

        //简答题入参校验
        if (questionFrom.getQuType() == 4 && StringUtils.isBlank(questionFrom.getAnswer())) {
            return Result.failed("简答题答案不能为空");
        }
        //非简答题入参校验
        List<Option> options = questionFrom.getOptions();
        if (questionFrom.getQuType() != 4 && (Objects.isNull(options) || options.size() < 2)) {
            return Result.failed("非简答题的试题选项不能少于两个");
        }

        Question question = questionConverter.fromToEntity(questionFrom);

        questionMapper.insertGetId(question);
        System.out.println("zhujian:" + question.getId());
        if (question.getQuType() == 4) {
            //简答题添加选项
            Option option = new Option();
            option.setQuId(question.getId());
            option.setContent(questionFrom.getAnswer());
            optionMapper.insert(option);
        } else {
            //非简答题添加选项
            //把新建试题获取的id，填入选项中
            options.forEach(option -> option.setQuId(question.getId()));
            optionMapper.insertBatch(options);
        }
        return Result.success("添加成功");

    }

    @Override
    @Transactional
    public Result<String> deleteBatchByIds(String ids) {
        List<Integer> list = Arrays.stream(ids.split(",")).map(Integer::parseInt).toList();
        //先删除选项
        optionMapper.deleteBatchByQuIds(list);
        //再删除试题
        questionMapper.deleteBatchIds(list);
        return Result.success("删除成功");
    }

    @Override
    public Result<IPage<QuestionVO>> pagingQuestion(Integer pageNum, Integer pageSize, String title, Integer type, Integer repoId) {
        IPage<QuestionVO> page = new Page<>(pageNum, pageSize);
        //教师只能查看自己创建的试题

        if ("role_teacher".equals(SecurityUtil.getRole())) {
            page = questionMapper.pagingQuestion(page, title, repoId, type, SecurityUtil.getUserId());
        } else {
            page = questionMapper.pagingQuestion(page, title, repoId, type, 0);
        }

        return Result.success(null, page);
    }
}
