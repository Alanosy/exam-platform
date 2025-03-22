package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.QuestionConverter;
import cn.org.alan.exam.mapper.ExerciseRecordMapper;
import cn.org.alan.exam.mapper.OptionMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.model.entity.ExerciseRecord;
import cn.org.alan.exam.model.entity.Option;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.form.question.QuestionExcelFrom;
import cn.org.alan.exam.model.form.question.QuestionFrom;
import cn.org.alan.exam.model.vo.question.QuestionVO;
import cn.org.alan.exam.service.IQuestionService;
import cn.org.alan.exam.utils.file.FileService;
import cn.org.alan.exam.utils.SecurityUtil;
import cn.org.alan.exam.utils.excel.ExcelUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * 试题管理实现类
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
    @Resource
    private ExerciseRecordMapper exerciseRecordMapper;

    @Override
    @Transactional
    public Result<String> addSingleQuestion(QuestionFrom questionFrom) {
        // 入参校验
        List<Option> options = questionFrom.getOptions();
        if (questionFrom.getQuType() != 4 && (Objects.isNull(options) || options.size() < 2)) {
            return Result.failed("非简答题的试题选项不能少于两个");
        }
        Question question = questionConverter.fromToEntity(questionFrom);
        // 开始添加题干
        questionMapper.insert(question);
        // 根据试题类型添加选项
        if (question.getQuType() == 4) {
            // 简答题添加选项
            Option option = questionFrom.getOptions().get(0);
            option.setQuId(question.getId());
            optionMapper.insert(option);
        } else {
            // 非简答题添加选项
            // 把新建试题获取的id，填入选项中
            options.forEach(option -> {
                option.setQuId(question.getId());
            });
            optionMapper.insertBatch(options);
        }
        return Result.success("单题添加成功");

    }

    @Override
    @Transactional
    public Result<String> deleteBatchByIds(String ids) {
        List<Integer> qIdList = Arrays.stream(ids.split(",")).map(Integer::parseInt).collect(java.util.stream.Collectors.toList());
        // 删除用户刷题记录表
        LambdaUpdateWrapper<ExerciseRecord> updateWrapper = new LambdaUpdateWrapper<ExerciseRecord>()
                .in(ExerciseRecord::getQuestionId, qIdList);
        exerciseRecordMapper.delete(updateWrapper);
        // 先删除选项
        optionMapper.deleteBatchIds(qIdList);
        // 再删除试题
        questionMapper.deleteBatchIds(qIdList);
        return Result.success("批量删除试题成功");
    }

    @Override
    public Result<IPage<QuestionVO>> pagingQuestion(Integer pageNum, Integer pageSize, String title, Integer type, Integer repoId) {
        IPage<QuestionVO> page = new Page<>(pageNum, pageSize);
        // 获取用户和角色代码
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        // 查询分页试题
        page = questionMapper.selectQuestionPage(page, userId, roleCode, title, type, repoId);
        return Result.success("分页查询试题成功", page);
    }

    @Override
    public Result<QuestionVO> querySingle(Integer id) {
        QuestionVO result = questionMapper.selectSingle(id);
        return Result.success("根据试题id获取单题详情成功", result);
    }

    @Override
    @Transactional
    public Result<String> updateQuestion(QuestionFrom questionFrom) {
        // 修改试题
        Question question = questionConverter.fromToEntity(questionFrom);
        questionMapper.updateById(question);
        // 修改选项
        List<Option> options = questionFrom.getOptions();
        for (Option option : options) {
            optionMapper.updateById(option);
        }
        return Result.success("修改试题成功");
    }

    @SneakyThrows(Exception.class)
    @Override
    @Transactional
    public Result<String> importQuestion(Integer id, MultipartFile file) {
        if (!ExcelUtils.isExcel(Objects.requireNonNull(file.getOriginalFilename()))) {
            throw new ServiceRuntimeException("该文件不是一个合法的Excel文件");
        }
        List<QuestionExcelFrom> questionExcelFroms = ExcelUtils.readMultipartFile(file, QuestionExcelFrom.class);
        // 类型转换
        List<QuestionFrom> list = QuestionExcelFrom.converterQuestionFrom(questionExcelFroms);
        for (QuestionFrom questionFrom : list) {
            Question question = questionConverter.fromToEntity(questionFrom);
            question.setRepoId(id);
            // 添加单题获取Id
            questionMapper.insert(question);
            // 批量添加选项
            List<Option> options = questionFrom.getOptions();
            final int[] count = {0};
            options.forEach(option -> {
                // 简答题答案默认给正确
                if (question.getQuType() == 4) {
                    option.setIsRight(1);
                }
                option.setSort(++count[0]);
                option.setQuId(question.getId());
            });
            // 避免简答题没有答案
            if (!options.isEmpty()) {
                optionMapper.insertBatch(options);
            }

        }
        return Result.success("导入试题成功");
    }

}
