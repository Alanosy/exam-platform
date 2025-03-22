package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.model.vo.repo.RepoListVO;
import cn.org.alan.exam.model.vo.repo.RepoVO;
import cn.org.alan.exam.model.vo.exercise.ExerciseRepoVO;
import cn.org.alan.exam.service.IRepoService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.aspectj.weaver.ast.Var;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 题库管理服务实现类
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
    @Resource
    private UserGradeMapper userGradeMapper;
    @Resource
    private UserMapper userMapper;


    @Override
    public Result<String> addRepo(Repo repo) {
        int row = repoMapper.insert(repo);
        if (row > 0) {
            return Result.success("新增题库成功");
        }
        throw new ServiceRuntimeException("添加题库条数<1");
    }

    @Override
    public Result<String> updateRepo(Repo repo, Integer id) {
        // 修改题库
        LambdaUpdateWrapper<Repo> updateWrapper = new LambdaUpdateWrapper<Repo>()
                .eq(Repo::getId, id)
                .set(Repo::getTitle, repo.getTitle())
                .set(Repo::getIsExercise, repo.getIsExercise());
        int row = repoMapper.update(updateWrapper);
        if (row > 0) {
            return Result.success("修改题库成功");
        }
        throw new ServiceRuntimeException("修改题库条数<1");
    }

    @Override
    @Transactional
    public Result<String> deleteRepoById(Integer id) {
        // 题库内试题清空所属题库id
        LambdaUpdateWrapper<Question> wrapper = new LambdaUpdateWrapper<Question>()
                .eq(Question::getRepoId, id)
                .set(Question::getRepoId, null);
        questionMapper.update(wrapper);
        // 删除题库
        boolean result = this.removeById(id);
        if (result) {
            return Result.success("删除题库成功");
        }
        throw new ServiceRuntimeException("删除题库条数<1");
    }

    @Override
    public Result<List<RepoListVO>> getRepoList(String repoTitle) {
        List<RepoListVO> list;
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        if (roleCode == 2) {
            list = repoMapper.selectRepoList(repoTitle, userId);
        } else {
            list = repoMapper.selectRepoList(repoTitle, 0);
        }
        return Result.success("根据用户id获取自己的题库获取成功", list);
    }

    @Override
    public Result<IPage<RepoVO>> pagingRepo(Integer pageNum, Integer pageSize, String title) {
        IPage<RepoVO> page = new Page<>(pageNum, pageSize);
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        if (roleCode == 2) {
            // 教师只查询自己的题库
            page = repoMapper.pagingRepo(page, title, userId);
        } else {
            // 管理员可以获取所有题库
            page = repoMapper.pagingRepo(page, title, 0);
        }
        return Result.success("题库分页查询成功", page);
    }

    @Override
    public Result<IPage<ExerciseRepoVO>> getRepo(Integer pageNum, Integer pageSize, String title) {
        IPage<ExerciseRepoVO> page = new Page<>(pageNum, pageSize);
        // 获取当前学生所在班级ID
        Integer gradeId = SecurityUtil.getGradeId();
        // 获得管理员列表
        List<Integer> adminList = userMapper.getAdminList();
        // 获取班级的所有老师用户ID
        List<Integer> userList = userGradeMapper.getUserListByGradeId(gradeId);
        userList.addAll(adminList);
        // 查询可以刷的题库，条件是没有删除的公开的是班级内老师的题库
        page = repoMapper.selectRepo(page, title, userList);
        return Result.success("分页获取可刷题库列表成功", page);
    }
}
