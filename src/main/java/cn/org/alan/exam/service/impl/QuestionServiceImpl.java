package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.QuestionConverter;
import cn.org.alan.exam.mapper.OptionMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.model.entity.Option;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.form.question.QuestionExcelFrom;
import cn.org.alan.exam.model.form.question.QuestionFrom;
import cn.org.alan.exam.model.vo.QuestionVO;
import cn.org.alan.exam.service.IQuestionService;
import cn.org.alan.exam.util.AliOSSUtil;
import cn.org.alan.exam.util.SecurityUtil;
import cn.org.alan.exam.util.excel.ExcelUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    private AliOSSUtil aliOSSUtil;


    @Override
    @Transactional
    public Result<String> addSingleQuestion(QuestionFrom questionFrom) {
        System.out.println("----------------");
        System.out.println(questionFrom);


        // 入参校验
        List<Option> options = questionFrom.getOptions();
        if (questionFrom.getQuType() != 4 && (Objects.isNull(options) || options.size() < 2)) {
            return Result.failed("非简答题的试题选项不能少于两个");
        }

        Question question = questionConverter.fromToEntity(questionFrom);

        questionMapper.insert(question);
        System.out.println("zhujian:" + question.getId());
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

    @Override
    @Transactional
    public Result<String> deleteBatchByIds(String ids) {
        List<Integer> list = Arrays.stream(ids.split(",")).map(Integer::parseInt).toList();
        // 先删除选项
        optionMapper.deleteBatchByQuIds(list);
        // 再删除试题
        questionMapper.deleteBatchIds(list);
        return Result.success("删除成功");
    }

    @Override
    public Result<IPage<QuestionVO>> pagingQuestion(Integer pageNum, Integer pageSize, String title, Integer type, Integer repoId) {
        IPage<QuestionVO> page = new Page<>(pageNum, pageSize);
        // 教师只能查看自己创建的试题

        if ("role_teacher".equals(SecurityUtil.getRole())) {
            page = questionMapper.pagingQuestion(page, title, repoId, type, SecurityUtil.getUserId());
        } else {
            page = questionMapper.pagingQuestion(page, title, repoId, type, 0);
        }

        return Result.success(null, page);
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
                if(question.getQuType() == 4){
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
        if (!aliOSSUtil.isImage(Objects.requireNonNull(file.getOriginalFilename()))) {
            return Result.failed("该文件不是常用图片格式(png、jpg、jpeg、bmp)");
        }
        if (aliOSSUtil.isOverSize(file)) {
            return Result.failed("图片大小不能超过50KB");
        }
        String url = aliOSSUtil.upload(file);
        if (StringUtils.isBlank(url)) {
            return Result.failed("图片上传失败");
        }
        return Result.success("图片上传成功", url);
    }
}
