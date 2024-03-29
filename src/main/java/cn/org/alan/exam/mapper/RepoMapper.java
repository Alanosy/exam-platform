package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.model.vo.RepoVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Repository
public interface RepoMapper extends BaseMapper<Repo> {

    /**
     * 分页查询题库
     * @param page 分页项
     * @param title 题库名
     * @param userId 用户名
     * @return 响应结果
     */
    IPage<RepoVO> pagingRepo(@Param("page") IPage<RepoVO> page, @Param("title") String title, @Param("userId") Integer userId);
}
