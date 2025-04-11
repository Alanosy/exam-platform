package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.Category;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.entity.Repo;
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
import java.util.List;
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
    private UserGradeMapper userGradeMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ICategoryService categoryService;

    @Override
    public Result<String> addRepo(Repo repo) {
        // 检查分类ID是否存在
        if (repo.getCategoryId() != null) {
            Category category = categoryService.getById(repo.getCategoryId());
            if (category == null) {
                return Result.failed("分类不存在");
            }
        }
        
        int row = repoMapper.insert(repo);
        if (row > 0) {
            return Result.success("新增题库成功");
        }
        throw new ServiceRuntimeException("添加题库条数<1");
    }

    @Override
    public Result<String> updateRepo(Repo repo, Integer id) {
        // 检查分类ID是否存在
        if (repo.getCategoryId() != null) {
            Category category = categoryService.getById(repo.getCategoryId());
            if (category == null) {
                return Result.failed("分类不存在");
            }
        }
        
        // 修改题库
        LambdaUpdateWrapper<Repo> updateWrapper = new LambdaUpdateWrapper<Repo>()
                .eq(Repo::getId, id)
                .set(Repo::getTitle, repo.getTitle())
                .set(Repo::getIsExercise, repo.getIsExercise())
                .set(repo.getCategoryId() != null, Repo::getCategoryId, repo.getCategoryId());
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
    public Result<IPage<RepoVO>> pagingRepo(Integer pageNum, Integer pageSize, String title, Integer categoryId) {
        IPage<RepoVO> page = new Page<>(pageNum, pageSize);
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        if (roleCode == 2) {
            page = repoMapper.pagingRepo(page, title, userId, categoryId);
        } else {
            // 管理员可以查看所有题库
            page = repoMapper.pagingRepo(page, title, 0, categoryId);
        }
        
        // 为每个题库设置分类信息
        List<RepoVO> records = page.getRecords();
        for (RepoVO vo : records) {
            if (vo.getCategoryId() != null) {
                // 查询分类信息
                Category category = categoryService.getById(vo.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
            }
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
        
        // 为每个题库设置分类信息
        List<ExerciseRepoVO> records = page.getRecords();
        for (ExerciseRepoVO vo : records) {
            // 查询题库对应的分类信息
            Repo repo = this.getById(vo.getId());
            if (repo != null && repo.getCategoryId() != null) {
                // 设置分类ID
                vo.setCategoryId(repo.getCategoryId());
                
                // 查询分类信息
                Category category = categoryService.getById(repo.getCategoryId());
                if (category != null) {
                    // 设置分类名称
                    vo.setCategoryName(category.getName());
                    
                    // 如果有父分类，设置父分类信息
                    if (category.getParentId() != null && category.getParentId() > 0) {
                        vo.setParentCategoryId(category.getParentId());
                        
                        // 查询父分类信息
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
    public Result<IPage<RepoVO>> getReposByCategory(Integer categoryId, Integer pageNum, Integer pageSize) {
        // 查询该分类下的所有子分类ID
        List<Integer> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId);
        
        // 如果是一级分类，还需要查询其下的所有二级分类
        LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.eq(Category::getParentId, categoryId);
        List<Category> childCategories = categoryService.list(categoryWrapper);
        if (!childCategories.isEmpty()) {
            List<Integer> childIds = childCategories.stream()
                    .map(Category::getId)
                    .collect(Collectors.toList());
            categoryIds.addAll(childIds);
        }
        
        // 查询题库
        Page<Repo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Repo> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Repo::getCategoryId, categoryIds)
               .orderByDesc(Repo::getCreateTime);
        
        // 如果是教师，只能查看自己创建的题库
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        if (roleCode == 2) {
            wrapper.eq(Repo::getUserId, userId);
        }
        
        IPage<Repo> repoPage = page(page, wrapper);
        
        // 转换为VO
        IPage<RepoVO> result = repoPage.convert(repo -> {
            RepoVO vo = new RepoVO();
            BeanUtils.copyProperties(repo, vo);
            
            // 设置分类名称
            if (repo.getCategoryId() != null) {
                Category category = categoryService.getById(repo.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
            }
            
            return vo;
        });
        
        return Result.success("根据分类查询题库成功", result);
    }
}