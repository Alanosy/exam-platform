package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.model.vo.RepoVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IRepoService extends IService<Repo> {

    /**
     * 添加题库
     *
     * @param repo 参数
     * @return 返回
     */
    Result<String> addRepo(Repo repo);

    /**
     * 修改题库
     *
     * @param repo 修改内容
     * @param id   路径参数题库id
     * @return 返回响应结果
     */
    Result<String> updateRepo(Repo repo, Integer id);

    /**
     * 根据题库id删除题库 并把试题所属题库清空
     *
     * @param id 题库id
     * @return 响应返回结果
     */
    Result<String> deleteRepoById(Integer id);

    /**
     * 根据用户id获取自己的题库 教师获取自己的，管理员可以获取所有
     *
     * @return 响应结果
     */
    Result<List<RepoVO>> getRepoList();

    /**
     * 分页查询题库
     *
     * @param pageNum  页码
     * @param pageSize 每页记录数
     * @param title    标题
     * @return 返回结果响应
     */
    Result<IPage<RepoVO>> pagingRepo(Integer pageNum, Integer pageSize, String title);

    /**
     * 分页获取可刷题库列表
     *
     * @param pageNum  页码
     * @param pageSize 每页记录数
     * @param title    题库名
     * @return 响应结果
     */
    Result<IPage<cn.org.alan.exam.model.vo.exercise.RepoVO>> getRepo(Integer pageNum, Integer pageSize, String title);
}
