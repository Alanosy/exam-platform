package cn.org.alan.exam.controller;


import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.model.vo.RepoVO;
import cn.org.alan.exam.service.IRepoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题库管理
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@RestController
@RequestMapping("/api/repo")
public class RepoController {

    @Resource
    private IRepoService iRepoService;

    /**
     * 添加题库，只有教师和管理员可以添加题库
     *
     * @param repo 添加题库的参数
     * @return 返回响应结果
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> addRepo(@Validated @RequestBody Repo repo) {
        //从token获取用户id，放入创建人id属性
        return iRepoService.addRepo(repo);
    }


    /**
     * 修改题库
     *
     * @param repo 传递参数
     * @return 返回响应
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> updateRepo(@Validated @RequestBody Repo repo, @PathVariable("id") Integer id) {
        return iRepoService.updateRepo(repo, id);
    }

    /**
     * 根据题库id删除题库
     *
     * @param id 题库id
     * @return 返回响应结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> deleteRepoById(@PathVariable("id") Integer id) {
        return iRepoService.deleteRepoById(id);
    }

    /**
     * 获取题库id和题库名，教师获取自己的题库，管理员获取所有题库
     * @param repoTitle 题库名称
     * @return 响应结果
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<RepoVO>> getRepoList(@RequestParam(value = "repoTitle",required = false) String repoTitle) {
        return iRepoService.getRepoList(repoTitle);
    }

    /**
     * 分页查询题库
     *
     * @param pageNum  页码
     * @param pageSize 每页记录数
     * @param title    题库名
     * @return 响应结果
     */
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<RepoVO>> pagingRepo(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                            @RequestParam(value = "title", required = false) String title) {
        return iRepoService.pagingRepo(pageNum, pageSize, title);
    }


}
