package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Repo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IRepoService extends IService<Repo> {

    /**
     * 添加题库
     * @param repo 参数
     * @return 返回
     */
    Result<String> addRepo(Repo repo);

    /**
     * 修改题库
     * @param repo 修改内容
     * @param id 路径参数题库id
     * @return 返回响应结果
     */
    Result<String> updateRepo(Repo repo, Integer id);

    /**
     * 根据题库id删除题库 并把试题所属题库清空
     * @param id 题库id
     * @return 响应返回结果
     */
    Result<String> deleteRepoById(Integer id);
}
