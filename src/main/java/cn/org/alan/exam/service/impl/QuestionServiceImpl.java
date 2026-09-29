package cn.org.alan.exam.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.alan.exam.common.cache.QuContentCacheService;
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
import cn.org.alan.exam.utils.BlankPlaceholderUtil;
import cn.org.alan.exam.utils.SecurityUtil;
import cn.org.alan.exam.utils.excel.ExcelUtils;
import lombok.SneakyThrows;

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
    @Resource
    private QuContentCacheService quContentCacheService;

    @Override
    @Transactional
    public Result<String> addSingleQuestion(QuestionFrom questionFrom) {
        // 入参校验
        List<Option> options = questionFrom.getOptions();
        Integer quType = questionFrom.getQuType();
        if (quType != null && quType == 5) {
            if (Objects.isNull(options) || options.isEmpty()) {
                return Result.failed("填空题至少需要一个空的答案");
            }
            for (Option option : options) {
                if (option.getContent() == null || option.getContent().trim().isEmpty()) {
                    return Result.failed("填空题每空答案不能为空");
                }
                option.setIsRight(1);
            }
            String validateMsg = BlankPlaceholderUtil.validate(questionFrom.getContent(), options.size());
            if (validateMsg != null) {
                return Result.failed(validateMsg);
            }
        } else if (quType != null && quType != 4 && (Objects.isNull(options) || options.size() < 2)) {
            return Result.failed("非简答题的试题选项不能少于两个");
        } else if (quType != null && quType == 4 && (Objects.isNull(options) || options.isEmpty())) {
            return Result.failed("简答题需要填写参考答案");
        }
        Question question = questionConverter.fromToEntity(questionFrom);
        if (question.getLevel() == null || question.getLevel() < 1 || question.getLevel() > 5) {
            question.setLevel(3);
        }
        // 新题排到题库末尾
        if (question.getRepoId() != null) {
            LambdaQueryWrapper<Question> sortWrapper = new LambdaQueryWrapper<Question>()
                    .eq(Question::getRepoId, question.getRepoId())
                    .orderByDesc(Question::getSort)
                    .last("limit 1");
            Question last = questionMapper.selectOne(sortWrapper);
            question.setSort(last == null || last.getSort() == null ? 1 : last.getSort() + 1);
        } else {
            question.setSort(1);
        }
        // 开始添加题干
        questionMapper.insert(question);
        // 根据试题类型添加选项
        if (question.getQuType() == 4) {
            // 简答题添加选项
            Option option = questionFrom.getOptions().get(0);
            option.setQuId(question.getId());
            optionMapper.insert(option);
        } else if (question.getQuType() == 5) {
            // 填空题：一空一条选项，按顺序写入 sort
            final int[] sort = {0};
            options.forEach(option -> {
                option.setQuId(question.getId());
                option.setIsRight(1);
                option.setSort(++sort[0]);
            });
            optionMapper.insertBatch(options);
        } else {
            // 客观题添加选项
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
        quContentCacheService.evictBatch(qIdList);
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
        List<Option> options = questionFrom.getOptions();
        Integer quType = questionFrom.getQuType();
        if (quType != null && quType == 5) {
            if (Objects.isNull(options) || options.isEmpty()) {
                return Result.failed("填空题至少需要一个空的答案");
            }
            for (Option option : options) {
                if (option.getContent() == null || option.getContent().trim().isEmpty()) {
                    return Result.failed("填空题每空答案不能为空");
                }
            }
            String validateMsg = BlankPlaceholderUtil.validate(questionFrom.getContent(), options.size());
            if (validateMsg != null) {
                return Result.failed(validateMsg);
            }
        }
        // 修改试题
        Question question = questionConverter.fromToEntity(questionFrom);
        if (question.getLevel() == null || question.getLevel() < 1 || question.getLevel() > 5) {
            question.setLevel(3);
        }
        questionMapper.updateById(question);
        // 处理选项更新
        if (quType != null && quType == 5) {
            // 填空题：先物理删除所有旧选项，再插入新选项
            optionMapper.physicalDeleteByQuId(question.getId());
            final int[] sort = {0};
            options.forEach(option -> {
                option.setId(null);
                option.setQuId(question.getId());
                option.setIsRight(1);
                option.setSort(++sort[0]);
            });
            optionMapper.insertBatch(options);
        } else if (options != null) {
            // 其他题型：根据 isDeleted 标记删除，更新或插入新选项
            for (Option option : options) {
                if (option.getId() != null && option.getIsDeleted() != null && option.getIsDeleted() == 1) {
                    // 标记为删除的选项，执行逻辑删除
                    optionMapper.deleteById(option.getId());
                } else if (option.getId() != null) {
                    // 更新现有选项
                    optionMapper.updateById(option);
                } else {
                    // 插入新选项
                    option.setQuId(question.getId());
                    optionMapper.insert(option);
                }
            }
        }
        quContentCacheService.evict(question.getId());
        return Result.success("修改试题成功");
    }

    @SneakyThrows(Exception.class)
    @Override
    @Transactional
    public Result<String> importQuestion(Integer id, MultipartFile file) {
        if (!ExcelUtils.isExcel(Objects.requireNonNull(file.getOriginalFilename()))) {
            throw new ServiceRuntimeException("该文件不是一个合法的Excel文件");
        }
        
        try {
            List<QuestionExcelFrom> questionExcelFroms = ExcelUtils.readMultipartFile(file, QuestionExcelFrom.class);
            // 类型转换
            List<QuestionFrom> list = QuestionExcelFrom.converterQuestionFrom(questionExcelFroms);
            
            for (QuestionFrom questionFrom : list) {
                Question question = questionConverter.fromToEntity(questionFrom);
                question.setRepoId(id);
                LambdaQueryWrapper<Question> sortWrapper = new LambdaQueryWrapper<Question>()
                        .eq(Question::getRepoId, id)
                        .orderByDesc(Question::getSort)
                        .last("limit 1");
                Question last = questionMapper.selectOne(sortWrapper);
                question.setSort(last == null || last.getSort() == null ? 1 : last.getSort() + 1);
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
        } catch (ServiceRuntimeException e) {
            // 捕获并返回业务异常，保留详细错误信息
            return Result.failed(e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常
            return Result.failed("导入试题失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> sortQuestion(Integer id, String direction) {
        Question current = questionMapper.selectById(id);
        if (current == null) {
            return Result.failed("试题不存在");
        }
        if (current.getRepoId() == null) {
            return Result.failed("试题未绑定题库，无法排序");
        }
        Integer currentSort = current.getSort() == null ? 0 : current.getSort();
        LambdaQueryWrapper<Question> neighborWrapper = new LambdaQueryWrapper<Question>()
                .eq(Question::getRepoId, current.getRepoId());
        if ("up".equalsIgnoreCase(direction)) {
            neighborWrapper.lt(Question::getSort, currentSort)
                    .orderByDesc(Question::getSort)
                    .last("limit 1");
        } else if ("down".equalsIgnoreCase(direction)) {
            neighborWrapper.gt(Question::getSort, currentSort)
                    .orderByAsc(Question::getSort)
                    .last("limit 1");
        } else {
            return Result.failed("direction 仅支持 up/down");
        }
        Question neighbor = questionMapper.selectOne(neighborWrapper);
        if (neighbor == null) {
            return Result.failed("up".equalsIgnoreCase(direction) ? "已经是第一题" : "已经是最后一题");
        }
        Integer neighborSort = neighbor.getSort() == null ? 0 : neighbor.getSort();
        Question updateCurrent = new Question();
        updateCurrent.setId(current.getId());
        updateCurrent.setSort(neighborSort);
        Question updateNeighbor = new Question();
        updateNeighbor.setId(neighbor.getId());
        updateNeighbor.setSort(currentSort);
        questionMapper.updateById(updateCurrent);
        questionMapper.updateById(updateNeighbor);
        return Result.success("排序调整成功");
    }

}
