package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.Category;
import cn.org.alan.exam.model.entity.GradeExercise;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.entity.Repo;
import cn.org.alan.exam.model.form.repo.RepoForm;
import cn.org.alan.exam.model.vo.repo.RepoListVO;
import cn.org.alan.exam.model.vo.repo.RepoVO;
import cn.org.alan.exam.model.vo.exercise.ExerciseRepoVO;
import cn.org.alan.exam.service.ICategoryService;
import cn.org.alan.exam.service.IRepoService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private GradeExerciseMapper gradeExerciseMapper;
    @Resource
    private UserGradeMapper userGradeMapper;
    @Resource
    private ICategoryService categoryService;

    @Override
    @Transactional
    public Result<String> addRepo(RepoForm repoForm) {
        if (repoForm.getCategoryId() != null) {
            Category category = categoryService.getById(repoForm.getCategoryId());
            if (category == null) {
                return Result.failed("分类不存在");
            }
        }

        List<Integer> gradeIdList = parseAndCheckGradeIds(repoForm.getGradeIds());
        Repo repo = new Repo();
        repo.setTitle(repoForm.getTitle());
        repo.setIsExercise(repoForm.getIsExercise());
        repo.setCategoryId(repoForm.getCategoryId());
        int row = repoMapper.insert(repo);
        if (row < 1) {
            throw new ServiceRuntimeException("添加题库条数<1");
        }
        LambdaUpdateWrapper<Repo> sortWrapper = new LambdaUpdateWrapper<Repo>()
                .eq(Repo::getId, repo.getId())
                .set(Repo::getSort, repo.getId());
        repoMapper.update(sortWrapper);
        saveRepoGrades(repo.getId(), gradeIdList);
        return Result.success("新增题库成功");
    }

    @Override
    @Transactional
    public Result<String> updateRepo(RepoForm repoForm, Integer id) {
        if (repoForm.getCategoryId() != null) {
            Category category = categoryService.getById(repoForm.getCategoryId());
            if (category == null) {
                return Result.failed("分类不存在");
            }
        }

        List<Integer> gradeIdList = parseAndCheckGradeIds(repoForm.getGradeIds());
        LambdaUpdateWrapper<Repo> updateWrapper = new LambdaUpdateWrapper<Repo>()
                .eq(Repo::getId, id)
                .set(Repo::getTitle, repoForm.getTitle())
                .set(Repo::getIsExercise, repoForm.getIsExercise())
                .set(repoForm.getCategoryId() != null, Repo::getCategoryId, repoForm.getCategoryId());
        int row = repoMapper.update(updateWrapper);
        if (row < 1) {
            throw new ServiceRuntimeException("修改题库条数<1");
        }
        gradeExerciseMapper.delRepoGrade(id);
        saveRepoGrades(id, gradeIdList);
        return Result.success("修改题库成功");
    }

    @Override
    @Transactional
    public Result<String> deleteRepoById(Integer id) {
        LambdaUpdateWrapper<Question> wrapper = new LambdaUpdateWrapper<Question>()
                .eq(Question::getRepoId, id)
                .set(Question::getRepoId, null);
        questionMapper.update(wrapper);
        gradeExerciseMapper.delRepoGrade(id);
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
    public Result<IPage<RepoVO>> pagingRepo(Integer pageNum, Integer pageSize, String title, Integer categoryId, Integer isExercise) {
        IPage<RepoVO> page = new Page<>(pageNum, pageSize);
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        if (roleCode == 2) {
            page = repoMapper.pagingRepo(page, title, userId, categoryId, isExercise);
        } else {
            page = repoMapper.pagingRepo(page, title, 0, categoryId, isExercise);
        }

        List<RepoVO> records = page.getRecords();
        for (RepoVO vo : records) {
            if (vo.getCategoryId() != null) {
                Category category = categoryService.getById(vo.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
            }

            LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
            questionWrapper.eq(Question::getRepoId, vo.getId());
            int count = questionMapper.selectCount(questionWrapper).intValue();
            vo.setQuestionCount(count);
        }
        fillGradeIds(records);

        return Result.success("题库分页查询成功", page);
    }

    @Override
    public Result<IPage<ExerciseRepoVO>> getRepo(Integer pageNum, Integer pageSize, String title, Integer categoryId) {
        IPage<ExerciseRepoVO> page = new Page<>(pageNum, pageSize);
        List<Integer> gradeIds = userGradeMapper.getStudentGradeIdList(SecurityUtil.getUserId());
        if (gradeIds == null || gradeIds.isEmpty()) {
            return Result.success("分页获取可刷题库列表成功", page);
        }
        page = repoMapper.selectRepo(page, title, gradeIds, categoryId);

        List<ExerciseRepoVO> records = page.getRecords();
        for (ExerciseRepoVO vo : records) {
            if (vo.getCategoryId() != null) {
                Category category = categoryService.getById(vo.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                    if (category.getParentId() != null && category.getParentId() > 0) {
                        vo.setParentCategoryId(category.getParentId());
                        Category parentCategory = categoryService.getById(category.getParentId());
                        if (parentCategory != null) {
                            vo.setParentCategoryName(parentCategory.getName());
                        }
                    }
                }
            }
        }

        return Result.success("分页获取可刷题库列表成功", page);
    }

    @Override
    public Result<List<cn.org.alan.exam.model.vo.exercise.ExerciseCategoryVO>> getExerciseCategories() {
        List<Integer> gradeIds = userGradeMapper.getStudentGradeIdList(SecurityUtil.getUserId());
        if (gradeIds == null || gradeIds.isEmpty()) {
            return Result.success("获取成功", Collections.emptyList());
        }
        List<Integer> categoryIds = repoMapper.selectExerciseCategoryIds(gradeIds);
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Result.success("获取成功", Collections.emptyList());
        }
        List<cn.org.alan.exam.model.vo.exercise.ExerciseCategoryVO> result = new ArrayList<>();
        for (Integer categoryId : categoryIds) {
            Category category = categoryService.getById(categoryId);
            if (category == null) {
                continue;
            }
            cn.org.alan.exam.model.vo.exercise.ExerciseCategoryVO vo =
                    new cn.org.alan.exam.model.vo.exercise.ExerciseCategoryVO();
            vo.setId(category.getId());
            vo.setName(category.getName());
            vo.setParentId(category.getParentId());
            if (category.getParentId() != null && category.getParentId() > 0) {
                Category parent = categoryService.getById(category.getParentId());
                if (parent != null) {
                    vo.setParentName(parent.getName());
                }
            }
            result.add(vo);
        }
        return Result.success("获取成功", result);
    }

    @Override
    public Result<IPage<RepoVO>> getReposByCategory(Integer categoryId, Integer pageNum, Integer pageSize) {
        List<Integer> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId);

        LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.eq(Category::getParentId, categoryId);
        List<Category> childCategories = categoryService.list(categoryWrapper);
        if (!childCategories.isEmpty()) {
            List<Integer> childIds = childCategories.stream()
                    .map(Category::getId)
                    .collect(Collectors.toList());
            categoryIds.addAll(childIds);
        }

        Page<Repo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Repo> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Repo::getCategoryId, categoryIds)
               .orderByAsc(Repo::getSort)
               .orderByAsc(Repo::getId);

        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        if (roleCode == 2) {
            wrapper.eq(Repo::getUserId, userId);
        }

        IPage<Repo> repoPage = page(page, wrapper);

        IPage<RepoVO> result = repoPage.convert(repo -> {
            RepoVO vo = new RepoVO();
            BeanUtils.copyProperties(repo, vo);

            if (repo.getCategoryId() != null) {
                Category category = categoryService.getById(repo.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
            }

            return vo;
        });
        fillGradeIds(result.getRecords());

        return Result.success("根据分类查询题库成功", result);
    }

    @Override
    @Transactional
    public Result<String> sortRepo(Integer id, String direction) {
        Repo current = repoMapper.selectById(id);
        if (current == null) {
            return Result.failed("题库不存在");
        }
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        if (roleCode != null && roleCode == 2 && !userId.equals(current.getUserId())) {
            return Result.failed("无权调整该题库");
        }
        boolean up;
        if ("up".equalsIgnoreCase(direction)) {
            up = true;
        } else if ("down".equalsIgnoreCase(direction)) {
            up = false;
        } else {
            return Result.failed("direction 仅支持 up/down");
        }

        LambdaQueryWrapper<Repo> neighborWrapper = new LambdaQueryWrapper<>();
        if (roleCode != null && roleCode == 2) {
            neighborWrapper.eq(Repo::getUserId, userId);
        }
        Integer currentSort = current.getSort() == null ? 0 : current.getSort();
        if (up) {
            neighborWrapper.lt(Repo::getSort, currentSort)
                    .orderByDesc(Repo::getSort)
                    .orderByDesc(Repo::getId)
                    .last("limit 1");
        } else {
            neighborWrapper.gt(Repo::getSort, currentSort)
                    .orderByAsc(Repo::getSort)
                    .orderByAsc(Repo::getId)
                    .last("limit 1");
        }
        Repo neighbor = repoMapper.selectOne(neighborWrapper);
        if (neighbor == null) {
            return Result.failed(up ? "已经是第一个题库" : "已经是最后一个题库");
        }

        Integer neighborSort = neighbor.getSort() == null ? 0 : neighbor.getSort();
        repoMapper.update(new LambdaUpdateWrapper<Repo>()
                .eq(Repo::getId, current.getId())
                .set(Repo::getSort, neighborSort));
        repoMapper.update(new LambdaUpdateWrapper<Repo>()
                .eq(Repo::getId, neighbor.getId())
                .set(Repo::getSort, currentSort));
        return Result.success("排序调整成功");
    }

    private List<Integer> parseAndCheckGradeIds(String gradeIds) {
        if (gradeIds == null || gradeIds.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> gradeIdList = Arrays.stream(gradeIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .distinct()
                .collect(Collectors.toList());
        if (gradeIdList.isEmpty()) {
            return gradeIdList;
        }
        Integer roleCode = SecurityUtil.getRoleCode();
        if (roleCode != null && roleCode == 2) {
            List<Integer> teacherGradeIds = userGradeMapper.getGradeIdListByUserId(SecurityUtil.getUserId());
            if (teacherGradeIds == null || teacherGradeIds.isEmpty()) {
                throw new ServiceRuntimeException("教师还没加入班级，无法绑定班级");
            }
            for (Integer gradeId : gradeIdList) {
                if (!teacherGradeIds.contains(gradeId)) {
                    throw new ServiceRuntimeException("只能绑定自己加入的班级");
                }
            }
        }
        return gradeIdList;
    }

    private void saveRepoGrades(Integer repoId, List<Integer> gradeIdList) {
        if (gradeIdList == null || gradeIdList.isEmpty()) {
            return;
        }
        gradeExerciseMapper.addRepoGrade(repoId, gradeIdList, SecurityUtil.getUserId());
    }

    private void fillGradeIds(List<RepoVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Integer> repoIds = records.stream().map(RepoVO::getId).collect(Collectors.toList());
        List<GradeExercise> relations = gradeExerciseMapper.selectByRepoIds(repoIds);
        Map<Integer, List<Integer>> gradeMap = relations.stream()
                .collect(Collectors.groupingBy(GradeExercise::getRepoId,
                        Collectors.mapping(GradeExercise::getGradeId, Collectors.toList())));
        for (RepoVO vo : records) {
            vo.setGradeIds(gradeMap.getOrDefault(vo.getId(), Collections.emptyList()));
        }
    }
}
