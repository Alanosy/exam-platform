package cn.org.alan.exam.controller;


import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.NoticeForm;
import cn.org.alan.exam.model.vo.NoticeVO;
import cn.org.alan.exam.service.INoticeService;
import cn.org.alan.exam.util.JwtUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 公告管理
 *
 * @author Alan
 * @since 2024-03-21
 */
@RestController
@RequestMapping("/api/notices")
public class NoticeController {
    @Resource
    private INoticeService noticeService;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private HttpServletRequest request;

    /**
     * 添加公告
     *
     * @param noticeForm
     * @return
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> addNotice(@RequestBody NoticeForm noticeForm) {
        return noticeService.addNotice( noticeForm);
    }

    /**
     * 删除公告
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> deleteNotice(@PathVariable("ids") String ids) {
        return noticeService.deleteNotice(ids);
    }

    /**
     * 修改公告
     *
     * @param id
     * @param noticeForm
     * @return
     */
    @PutMapping("/{id}")
    public Result<String> updateNotice(@PathVariable("id") String id, @RequestBody NoticeForm noticeForm) {
        return noticeService.updateNotice(id, noticeForm);
    }

    /**
     * 教师分页查找
     *
     * @param pageNum
     * @param pageSize
     * @param title
     * @return
     */
    @GetMapping("/paging")
    public Result<IPage<NoticeVO>> getNotice(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                             @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                             @RequestParam(value = "title", required = false) String title) {
        return noticeService.getNotice( pageNum, pageSize, title);

    }
    @GetMapping("/new")
    public Result<IPage<NoticeVO>> getNewNotice(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize){
        return noticeService.getNewNotice(pageNum,pageSize);
    }

}
