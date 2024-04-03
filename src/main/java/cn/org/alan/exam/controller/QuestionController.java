package cn.org.alan.exam.controller;


import cn.org.alan.exam.common.group.QuestionGroup;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.QuestionFrom;
import cn.org.alan.exam.model.vo.QuestionVO;
import cn.org.alan.exam.service.IQuestionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 试题管理
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {


    @Resource
    private IQuestionService iQuestionService;

    /**
     * 单题添加
     *
     * @param questionFrom 传参
     * @return 响应
     */
    @PostMapping("/single")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> addSingleQuestion(@Validated(QuestionGroup.QuestionAddGroup.class) @RequestBody QuestionFrom questionFrom) {
        return iQuestionService.addSingleQuestion(questionFrom);
    }

    /**
     * 批量删除试题
     *
     * @param ids 试题id
     * @return 相应
     */
    @DeleteMapping("/batch/{ids}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> deleteBatchQuestion(@PathVariable("ids") String ids) {
        return iQuestionService.deleteBatchByIds(ids);
    }

    /**
     * 分页查询试题
     *
     * @param pageNum  页码
     * @param pageSize 每页记录数
     * @param content    试题名
     * @param repoId   题库id
     * @param type     试题类型
     * @return 响应
     */
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<QuestionVO>> pagingQuestion(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                    @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                    @RequestParam(value = "content", required = false) String content,
                                                    @RequestParam(value = "repoId", required = false) Integer repoId,
                                                    @RequestParam(value = "type", required = false) Integer type) {
        return iQuestionService.pagingQuestion(pageNum, pageSize, content,type,repoId);
    }
}
