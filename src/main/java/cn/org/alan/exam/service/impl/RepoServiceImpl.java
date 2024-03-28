package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.mapper.RepoMapper;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.service.IRepoService;
import cn.org.alan.exam.util.DateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Objects;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
public class RepoServiceImpl extends ServiceImpl<RepoMapper, Repo> implements IRepoService {


    @Resource
    private RepoMapper repoMapper;
    @Resource
    private QuestionMapper questionMapper;


    @Override
    public Result<String> addRepo(Repo repo) {

        //题库名重复判断
        LambdaQueryWrapper<Repo> queryWrapper = new LambdaQueryWrapper<Repo>().eq(Repo::getTitle, repo.getTitle());
        if (Objects.nonNull(repoMapper.selectOne(queryWrapper))){
            return Result.failed("保存失败,题库名已存在");
        }

        //添加题库
        repo.setCreateTime(DateTimeUtil.getDateTime());
        int result = repoMapper.insert(repo);
        if (result > 0) {
            return Result.success("保存成功");
        }
        return Result.failed("保存失败");

    }

    @Override
    public Result<String> updateRepo(Repo repo, Integer id) {

        //题库名重复判断
        LambdaQueryWrapper<Repo> queryWrapper = new LambdaQueryWrapper<Repo>().eq(Repo::getTitle, repo.getTitle());
        if (Objects.nonNull(repoMapper.selectOne(queryWrapper))){
            return Result.failed("修改失败,题库名已存在");
        }

        //修改题库
        LambdaUpdateWrapper<Repo> updateWrapper = new LambdaUpdateWrapper<Repo>()
                .eq(Repo::getId, id).set(Repo::getTitle, repo.getTitle());
        int result = repoMapper.update(updateWrapper);
        if (result > 0) {
            return Result.success("修改成功");
        }
        return Result.failed("修改失败");

    }

    @Override
    @Transactional
    public Result<String> deleteRepoById(Integer id) {

        //题库内试题清空所属题库id
        LambdaUpdateWrapper<Question> wrapper = new LambdaUpdateWrapper<Question>().set(Question::getRepoId, id);
        questionMapper.update(wrapper);
        //删除题库
        int result = repoMapper.deleteById(id);
        if (result>0){
            return Result.success("删除成功");
        }
        return Result.failed("删除失败");
    }
}
