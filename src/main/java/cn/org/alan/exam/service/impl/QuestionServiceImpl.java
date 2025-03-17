package cn.org.alan.exam.service.impl;

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
import cn.org.alan.exam.model.vo.QuestionVO;
import cn.org.alan.exam.service.IQuestionService;
import cn.org.alan.exam.util.FileService;
import cn.org.alan.exam.util.impl.CacheClient;
import cn.org.alan.exam.util.impl.SecurityUtil;
import cn.org.alan.exam.util.excel.ExcelUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 服务实现类
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
    private FileService fileService;
    @Resource
    private ExerciseRecordMapper exerciseRecordMapper;

    /**
     * 单题添加
     * @param questionFrom 传参
     * @return
     */
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
        return Result.success("添加成功");

    }

    /**
     * 批量删除试题
     * @param ids 试题id
     * @return
     */
    @Override
    @Transactional
    public Result<String> deleteBatchByIds(String ids) {
        List<Integer> list = Arrays.stream(ids.split(",")).map(Integer::parseInt).toList();

        // 删除用户刷题记录表
        LambdaUpdateWrapper<ExerciseRecord> updateWrapper =
                new LambdaUpdateWrapper<ExerciseRecord>().in(ExerciseRecord::getQuestionId, list);
        int delete = exerciseRecordMapper.delete(updateWrapper);
        // 先删除选项
        optionMapper.deleteBatchByQuIds(list);
        // 再删除试题
        questionMapper.deleteBatchIdsQu(list);
        return Result.success("删除成功");
    }

    /**
     * 分页查询试题
     * @param pageNum  页码
     * @param pageSize 每页记录数
     * @param title  试题名
     * @param type     试题类型
     * @param repoId   题库id
     * @return
     */
    @Override
    public Result<IPage<QuestionVO>> pagingQuestion(Integer pageNum, Integer pageSize, String title, Integer type, Integer repoId) {
        IPage<QuestionVO> page = new Page<>(pageNum, pageSize);
        // 获取用户和角色代码
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        // 查询分页试题
        page = questionMapper.selectQuestionPage(page, userId, roleCode, title, type, repoId);
        return Result.success("查询成功", page);
    }

    /**
     * 根据试题id获取单题详情
     * @param id 试题id
     * @return
     */
    @Override
    public Result<QuestionVO> querySingle(Integer id) {
        QuestionVO result = questionMapper.selectSingle(id);
        return Result.success("查询成功",result);
    }

    /**
     * 修改试题
     * @param questionFrom 需要修改的试题
     * @return
     */
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
        return Result.success("修改成功");
    }

    /**
     * 批量导入试题
     * @param id 题库Id
     * @param file Excel文件
     * @return
     */
    @SneakyThrows(Exception.class)
    @Override
    @Transactional
    public Result<String> importQuestion(Integer id, MultipartFile file) {
        if (!ExcelUtils.isExcel(Objects.requireNonNull(file.getOriginalFilename()))) {
            return Result.failed("该文件不是一个合法的Excel文件");
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
        return Result.success("导入成功");
    }

    @SneakyThrows(IOException.class)
    @Override
    public Result<String> uploadImage(MultipartFile file) {
        if (!fileService.isImage(Objects.requireNonNull(file.getOriginalFilename()))) {
            return Result.failed("该文件不是常用图片格式(png、jpg、jpeg、bmp)");
        }
        if (fileService.isOverSize(file)) {
            return Result.failed("图片大小不能超过50KB");
        }
        String url = fileService.upload(file);
        if (StringUtils.isBlank(url)) {
            return Result.failed("图片上传失败");
        }
        return Result.success("图片上传成功", url);
    }
}
