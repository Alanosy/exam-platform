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
    @Resource
    private CacheClient cacheClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public Result<String> addSingleQuestion(QuestionFrom questionFrom) {
        // 入参校验
        List<Option> options = questionFrom.getOptions();
        if (questionFrom.getQuType() != 4 && (Objects.isNull(options) || options.size() < 2)) {
            return Result.failed("非简答题的试题选项不能少于两个");
        }

        Question question = questionConverter.fromToEntity(questionFrom);

        questionMapper.insert(question);
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
        if (question.getId() != null) { // 确保ID有效
            // 如果是更新操作，先从缓存中移除旧数据，然后重新放入最新的数据
            stringRedisTemplate.delete("cache:question:pagingQuestion:"+question.getId().toString()); // 删除旧缓存
            //
            // QuestionVO questionVO = questionConverter.QuestionToQuestionVO(question); // 转换为视图对象
            // Map<Integer, QuestionVO> map = Map.of(questionVO.getId(), questionVO);
            // cacheClient.batchPut("cache:question:pagingQuestion:",map,10L,TimeUnit.MINUTES); // 存储新数据
        }
        return Result.success("添加成功");

    }

    @Override
    @Transactional
    public Result<String> deleteBatchByIds(String ids) {
        List<Integer> list = Arrays.stream(ids.split(",")).map(Integer::parseInt).toList();

        //删除用户刷题记录表
        LambdaUpdateWrapper<ExerciseRecord> updateWrapper =
                new LambdaUpdateWrapper<ExerciseRecord>().in(ExerciseRecord::getQuestionId, list);
        int delete = exerciseRecordMapper.delete(updateWrapper);
        // 先删除选项
        optionMapper.deleteBatchByQuIds(list);
        // 再删除试题
        questionMapper.deleteBatchIdsQu(list);
        list.forEach(id->{
            stringRedisTemplate.delete("cache:question:pagingQuestion:"+id);
        });

        return Result.success("删除成功");
    }

    @Override
    public Result<IPage<QuestionVO>> pagingQuestion(Integer pageNum, Integer pageSize, String title, Integer type, Integer repoId) {
        Integer userId = null;
        if ("role_teacher".equals(SecurityUtil.getRole())) {
            userId = SecurityUtil.getUserId();
        } else {
            userId = 0;
        }
        // 查询满足条件的总记录数
        int total = questionMapper.countByCondition(userId, title,type,repoId); // 假设gradeMapper中实现了根据条件计数的方法
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询分页ID列表
        List<Integer> quIds = questionMapper.selectQuestionIdsPage(userId, title,type,repoId, offset, pageSize);

        // 批量从缓存中获取GradeVO对象
        Map<Integer, QuestionVO> cachedQuestionsMap = cacheClient.batchGet("cache:question:pagingQuestion:",quIds, QuestionVO.class);

        // 确定未命中的ID列表
        List<Integer> missIds = new ArrayList<>();
        for (Integer id : quIds) {
            if (!cachedQuestionsMap.containsKey(id)) {
                missIds.add(id);
            }
        }

        // 如果有未命中的ID，从数据库批量查询并更新缓存
        if (!missIds.isEmpty()) {
            List<QuestionVO> missedGrades = questionMapper.batchSelectByIds(missIds);
            // 假设GradeVO的ID为getId()，使用Collectors.toMap转换
            Map<Integer, QuestionVO> missedGradesMap = missedGrades.stream()
                    .collect(Collectors.toMap(QuestionVO::getId, Function.identity()));
            // 更新缓存
            cacheClient.batchPut("cache:question:pagingQuestion:",missedGradesMap,10L, TimeUnit.MINUTES);
            // 合并缓存结果
            cachedQuestionsMap.putAll(missedGradesMap);
        }

        // 根据ID列表从缓存中获取完整的GradeVO对象列表
        List<QuestionVO> finalResult = new ArrayList<>(quIds.size());
        for (Integer id : quIds) {
            finalResult.add(cachedQuestionsMap.get(id));
        }

        // 构建并返回IPage对象
        IPage<QuestionVO> resultPage = new Page<>(pageNum, pageSize, Long.valueOf(total));
        resultPage.setRecords(finalResult);

        return Result.success(null, resultPage);
    }

    @Override
    public Result<QuestionVO> querySingle(Integer id) {
        return Result.success(null, questionMapper.selectSingle(id));
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
        if (question.getId() != null) { // 确保ID有效
            // 如果是更新操作，先从缓存中移除旧数据，然后重新放入最新的数据
            stringRedisTemplate.delete("cache:question:pagingQuestion:"+question.getId().toString()); // 删除旧缓存
            // QuestionVO questionVO = questionConverter.QuestionToQuestionVO(question); // 转换为视图对象
            // Map<Integer, QuestionVO> map = Map.of(questionVO.getId(), questionVO);
            // cacheClient.batchPut("cache:question:pagingQuestion:",map,10L,TimeUnit.MINUTES); // 存储新数据
        }
        return Result.success("修改成功");
    }

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
                //简答题答案默认给正确
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
            if (question.getId() != null) { // 确保ID有效
                // 如果是更新操作，先从缓存中移除旧数据，然后重新放入最新的数据
                stringRedisTemplate.delete("cache:question:pagingQuestion:"+question.getId().toString()); // 删除旧缓存
                // QuestionVO questionVO = questionConverter.QuestionToQuestionVO(question); // 转换为视图对象
                // Map<Integer, QuestionVO> map = Map.of(questionVO.getId(), questionVO);
                // cacheClient.batchPut("cache:question:pagingQuestion:",map,10L,TimeUnit.MINUTES); // 存储新数据
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
