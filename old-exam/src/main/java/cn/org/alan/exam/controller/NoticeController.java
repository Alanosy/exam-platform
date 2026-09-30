package cn.org.alan.exam.controller;


import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.notice.NoticeForm;
import cn.org.alan.exam.model.vo.notice.NoticeVO;
import cn.org.alan.exam.service.INoticeService;
import com.baomidou.mybatisplus.core.metadata.IPage;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 公告管理
 *
 * @author Alan
 * @since 2024-03-21
 */
@Api(tags = "公告管理相关接口")
@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    @Resource
    private INoticeService noticeService;

    /**
     * 添加公告
     *
     * @param noticeForm
     * @return
     */
    @ApiOperation("添加公告")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> addNotice(@Validated @RequestBody NoticeForm noticeForm) {
        return noticeService.addNotice(noticeForm);
    }

    /**
     * 删除公告
     *
     * @param ids
     * @return
     */
    @ApiOperation("删除公告")
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> deleteNotice(@PathVariable("ids") @NotBlank String ids) {
        return noticeService.deleteNotice(ids);
    }

    /**
     * 修改公告
     *
     * @param noticeId 公告ID
     * @param noticeForm
     * @return
     */
    @ApiOperation("修改公告")
    @PutMapping("/{noticeId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> updateNotice(@PathVariable("noticeId") @NotBlank Integer noticeId, @Validated @RequestBody NoticeForm noticeForm) {
        return noticeService.updateNotice(noticeId, noticeForm);
    }

    /**
     * 教师分页查找
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param title
     * @return
     */
    @ApiOperation("教师分页查找")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<NoticeVO>> getNotice(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                             @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                             @RequestParam(value = "title", required = false) String title) {
        return noticeService.getNotice(pageNum, pageSize, title);
    }

    /**
     * 获取最新消息
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return
     */
    @ApiOperation("获取最新消息")
    @GetMapping("/new")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<IPage<NoticeVO>> getNewNotice(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return noticeService.getNewNotice(pageNum, pageSize);
    }
}
