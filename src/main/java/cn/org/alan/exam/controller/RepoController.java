package cn.org.alan.exam.controller;


import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.service.IRepoService;
import cn.org.alan.exam.util.DateTimeUtil;
import cn.org.alan.exam.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 题库管理
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@RestController
@RequestMapping("/repo")
public class RepoController {


    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private IRepoService iRepoService;
    @Resource
    private HttpServletRequest request;


    /**
     * 添加题库，只有教师和管理员可以添加题库
     *
     * @param repo 添加题库的参数
     * @return 返回响应结果
     */
    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> addRepo(@RequestBody Repo repo) {
        //从token获取用户id，放入创建人id属性
        repo.setUserId(jwtUtil.getUserId(request));
        return iRepoService.addRepo(repo);
    }


    /**
     * 修改题库
     *
     * @param repo 传递参数
     * @return 返回响应
     */
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> updateRepo(@RequestBody Repo repo, @PathVariable("id") Integer id) {
        return iRepoService.updateRepo(repo, id);
    }

    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> deleteRepoById(@PathVariable("id") Integer id){
        return iRepoService.deleteRepoById(id);
    }

}
