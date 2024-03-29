package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.AppException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.RepoConverter;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.mapper.RepoMapper;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.model.vo.RepoVO;
import cn.org.alan.exam.service.IRepoService;
import cn.org.alan.exam.util.DateTimeUtil;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
public class RepoServiceImpl extends ServiceImpl<RepoMapper, Repo> implements IRepoService {


    @Resource
    private RepoMapper repoMapper;
    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private RepoConverter repoConverter;


    @Override
    public Result<String> addRepo(Repo repo) {

        repo.setCreateTime(DateTimeUtil.getDateTime());
        repo.setUserId(SecurityUtil.getUserId());
        repoMapper.insert(repo);

        return Result.success("保存成功");


    }

    @Override
    public Result<String> updateRepo(Repo repo, Integer id) {

        //修改题库
        LambdaUpdateWrapper<Repo> updateWrapper = new LambdaUpdateWrapper<Repo>()
                .eq(Repo::getId, id).set(Repo::getTitle, repo.getTitle());

        repoMapper.update(updateWrapper);

        return Result.success("修改成功");


    }

    @Override
    @Transactional
    public Result<String> deleteRepoById(Integer id) {

        //题库内试题清空所属题库id
        LambdaUpdateWrapper<Question> wrapper = new LambdaUpdateWrapper<Question>().set(Question::getRepoId, id);
        questionMapper.update(wrapper);
        //删除题库
        int result = repoMapper.deleteById(id);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.failed("删除失败");
    }

    @Override
    public Result<List<RepoVO>> getRepoList() {
        LambdaQueryWrapper<Repo> wrapper = new LambdaQueryWrapper<Repo>().select(Repo::getId, Repo::getTitle);
        if ("role_teacher".equals(SecurityUtil.getRole())) {
            wrapper.eq(Repo::getUserId, SecurityUtil.getUserId());
        }
        List<Repo> repos = repoMapper.selectList(wrapper);
        return Result.success(null, repoConverter.listEntityToVo(repos));
    }

    @Override
    public Result<IPage<RepoVO>> pagingRepo(Integer pageNum, Integer pageSize, String title) {

        IPage<RepoVO> page = new Page<>(pageNum, pageSize);

        if ("role_teacher".equals(SecurityUtil.getRole())) {
            //教师只查询自己的题库
            page = repoMapper.pagingRepo(page, title, SecurityUtil.getUserId());
        } else {
            //管理员可以获取所有题库
            page = repoMapper.pagingRepo(page, title, 0);
        }

        return Result.success(null, page);
    }
}
