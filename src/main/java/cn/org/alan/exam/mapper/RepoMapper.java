package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.model.vo.repo.RepoListVO;
import cn.org.alan.exam.model.vo.repo.RepoVO;
import cn.org.alan.exam.model.vo.exercise.ExerciseRepoVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 题库表 Mapper 接口
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface RepoMapper extends BaseMapper<Repo> {

    /**
     * 分页查询题库
     *
     * @param page   分页项
     * @param title  题库名
     * @param userId 用户名
     * @return 响应结果
     */
    IPage<RepoVO> pagingRepo(@Param("page") IPage<RepoVO> page, @Param("title") String title,
                             @Param("userId") Integer userId);

    /**
     * 分页获取可刷题库列表
     *
     * @param page     分页信息
     * @param title    题库名
     * @param userList 用户Id集
     * @return 结果
     */
    IPage<ExerciseRepoVO> selectRepo(IPage<ExerciseRepoVO> page,
                                     String title, List<Integer> userList);

    /**
     * 查询题库列表
     *
     * @param repoTitle 题库标题
     * @param userId    用户ID
     * @return 结果集
     */
    List<RepoListVO> selectRepoList(String repoTitle, int userId);

    /**
     * 查找刷题记录
     *
     * @param repoPage 分页page对象
     * @param userId   用户Id
     * @param repoName 题库名称
     * @return 分页结果
     */
    Page<Repo> selectUserExerciseRecord(Page<Repo> repoPage, Integer userId, String repoName);

}
