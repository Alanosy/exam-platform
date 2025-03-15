package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.ExerciseRecordMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.mapper.RepoMapper;
import cn.org.alan.exam.mapper.UserGradeMapper;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.model.entity.UserGrade;
import cn.org.alan.exam.model.vo.repo.RepoListVO;
import cn.org.alan.exam.model.vo.repo.RepoVO;
import cn.org.alan.exam.model.vo.exercise.ExerciseRepoVO;
import cn.org.alan.exam.service.IRepoService;
import cn.org.alan.exam.util.impl.SecurityUtil;
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
    private ExerciseRecordMapper exerciseRecordMapper;
    @Resource
    private UserGradeMapper userGradeMapper;


    @Override
    public Result<String> addRepo(Repo repo) {
        int row = repoMapper.insert(repo);
        if(row>0){
            return Result.success("保存成功");
        }
        return Result.failed("保存失败");
    }

    @Override
    public Result<String> updateRepo(Repo repo, Integer id) {
        //修改题库
        LambdaUpdateWrapper<Repo> updateWrapper = new LambdaUpdateWrapper<Repo>()
                .eq(Repo::getId, id).set(Repo::getTitle, repo.getTitle()).set(Repo::getIsExercise,repo.getIsExercise());

        repoMapper.update(updateWrapper);
        return Result.success("修改成功");
    }

    @Override
    @Transactional
    public Result<String> deleteRepoById(Integer id) {

        //题库内试题清空所属题库id
        LambdaUpdateWrapper<Question> wrapper = new LambdaUpdateWrapper<Question>()
                .eq(Question::getRepoId, id)
                .set(Question::getRepoId, null);
        questionMapper.update(wrapper);
        //删除题库
        LambdaUpdateWrapper<Repo> repoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        repoLambdaUpdateWrapper.eq(Repo::getId,id)
                .set(Repo::getIsDeleted,1);
        int result = repoMapper.update(repoLambdaUpdateWrapper);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.failed("删除失败");
    }

    @Override
    public Result<List<RepoListVO>> getRepoList(String repoTitle) {
        List<RepoListVO> list;
        if ("role_teacher".equals(SecurityUtil.getRole())) {
            list = repoMapper.selectRepoList(repoTitle, SecurityUtil.getUserId());
        } else {
            list = repoMapper.selectRepoList(repoTitle, 0);
        }
        return Result.success("获取成功", list);
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

    /**
     * 查询可以刷的题库
     * @param pageNum  页码
     * @param pageSize 每页记录数
     * @param title    题库名
     * @return
     */
    @Override
    public Result<IPage<ExerciseRepoVO>> getRepo(Integer pageNum, Integer pageSize, String title) {
        IPage<ExerciseRepoVO> page = new Page<>(pageNum, pageSize);
        // 获取当前学生所在班级ID
        Integer gradeId = SecurityUtil.getGradeId();
        // 获取班级的所有老师用户ID
        List<Integer> userList = userGradeMapper.getUserListByGradeId(gradeId);
        // 查询可以刷的题库，条件是没有删除的公开的是班级内老师的题库
        page = repoMapper.selectRepo(page, title,userList);
        return Result.success("查询成功", page);
    }
}
