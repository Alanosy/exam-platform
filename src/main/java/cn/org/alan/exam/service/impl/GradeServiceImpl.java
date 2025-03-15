package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.GradeConverter;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.UserGrade;
import cn.org.alan.exam.model.form.GradeForm;
import cn.org.alan.exam.model.vo.GradeVO;
import cn.org.alan.exam.service.IGradeService;
import cn.org.alan.exam.util.impl.CacheClient;
import cn.org.alan.exam.util.impl.ClassTokenGenerator;
import cn.org.alan.exam.util.impl.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 班级服务实现类
 *
 * @author Alan
 * @since 2024-03-21
 */
@Service
public class GradeServiceImpl extends ServiceImpl<GradeMapper, Grade> implements IGradeService {
    @Resource
    private GradeMapper gradeMapper;
    @Resource
    private ExamMapper examMapper;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private GradeConverter gradeConverter;
    @Resource
    private UserMapper userMapper;
    @Resource
    private IGradeService gradeService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CacheClient cacheClient;
    @Resource
    private UserGradeMapper userGradeMapper;

    @Override
    @Transactional
    public Result<String> addGrade(GradeForm gradeForm) {
        // 生成班级口令
        gradeForm.setCode(ClassTokenGenerator.generateClassToken(18));
        // 实体转换
        Grade grade = gradeConverter.formToEntity(gradeForm);
        // 添加数据
        int rows = gradeMapper.insert(grade);
        if (rows == 0) {
            return Result.failed("添加失败");
        }
        // 更新缓存
        if (grade.getId() != null) { // 确保ID有效
            // 如果是更新操作，先从缓存中移除旧数据，然后重新放入最新的数据
            stringRedisTemplate.delete("cache:grade:getPaging:"+grade.getId().toString()); // 删除旧缓存
            // GradeVO updatedGradeVO = gradeConverter.GradeToGradeVO(grade); // 转换为视图对象
            // Map<Integer, GradeVO> map = Map.of(updatedGradeVO.getId(), updatedGradeVO);
            // cacheClient.batchPut("cache:grade:getPaging:",map,10L,TimeUnit.MINUTES); // 存储新数据
        }
        stringRedisTemplate.delete("cache:grade:getAllGrade:"+SecurityUtil.getUserId());
        return Result.success("添加成功");
    }

    @Override
    @Transactional
    public Result<String> updateGrade(Integer id, GradeForm gradeForm) {
        // 建立更新条件
        LambdaUpdateWrapper<Grade> gradeUpdateWrapper = new LambdaUpdateWrapper<>();
        gradeUpdateWrapper
                .set(Grade::getGradeName, gradeForm.getGradeName())
                .eq(Grade::getId, id);
        // 更新班级
        int rows = gradeMapper.update(gradeUpdateWrapper);
        if (rows == 0) {
            return Result.failed("修改失败");
        }
        Grade byId = getById(id);
        // 更新缓存
        if (byId.getId() != null) { // 确保ID有效
            // 如果是更新操作，先从缓存中移除旧数据，然后重新放入最新的数据
            stringRedisTemplate.delete("cache:grade:getPaging:"+byId.getId().toString()); // 删除旧缓存
            // GradeVO updatedGradeVO = gradeConverter.GradeToGradeVO(byId); // 转换为视图对象
            // Map<Integer, GradeVO> map = Map.of(updatedGradeVO.getId(), updatedGradeVO);
            // cacheClient.batchPut("cache:grade:getPaging:",map,10L,TimeUnit.MINUTES); // 存储新数据
        }
        stringRedisTemplate.delete("cache:grade:getAllGrade:"+SecurityUtil.getUserId());
        return Result.success("修改成功");
    }

    @Override
    @Transactional
    public Result<String> deleteGrade(Integer id) {
        // 删除班级
        LambdaUpdateWrapper<Grade> gradeLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        gradeLambdaUpdateWrapper.eq(Grade::getId,id)
                .set(Grade::getIsDeleted,1);
        int rows = gradeMapper.update(gradeLambdaUpdateWrapper);
        if (rows == 0) {
            return Result.failed("删除失败");
        }
        // 删除缓存
        stringRedisTemplate.delete("cache:grade:getPaging:"+id.toString());
        stringRedisTemplate.delete("cache:grade:getAllGrade:"+SecurityUtil.getUserId());
        return Result.success("删除成功");
    }

    @Override
    public Result<IPage<GradeVO>> getPaging(Integer pageNum, Integer pageSize, String gradeName) {
        // 获取角色
        Integer role = 0;
        if("role_teacher".equals(SecurityUtil.getRole())){
            role = 2;
        }
        // 查询满足条件的总记录数
        int total = gradeMapper.countByCondition(SecurityUtil.getUserId(), gradeName,role); // 假设gradeMapper中实现了根据条件计数的方法
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询分页ID列表
        List<Integer> gradeIds = gradeMapper.selectGradeIdsPage(SecurityUtil.getUserId(), role ,gradeName, offset, pageSize);

        // 批量从缓存中获取GradeVO对象
        Map<Integer, GradeVO> cachedGradesMap = cacheClient.batchGet("cache:grade:getPaging:",gradeIds, GradeVO.class);

        // 确定未命中的ID列表
        List<Integer> missIds = new ArrayList<>();
        for (Integer id : gradeIds) {
            if (!cachedGradesMap.containsKey(id)) {
                missIds.add(id);
            }
        }

        // 如果有未命中的ID，从数据库批量查询并更新缓存
        if (!missIds.isEmpty()) {
            List<GradeVO> missedGrades = gradeMapper.batchSelectByIds(missIds);
            // 假设GradeVO的ID为getId()，使用Collectors.toMap转换
            Map<Integer, GradeVO> missedGradesMap = missedGrades.stream()
                    .collect(Collectors.toMap(GradeVO::getId, Function.identity()));
            // 更新缓存
            cacheClient.batchPut("cache:grade:getPaging:",missedGradesMap,10L,TimeUnit.MINUTES);
            // 合并缓存结果
            cachedGradesMap.putAll(missedGradesMap);
        }

        // 根据ID列表从缓存中获取完整的GradeVO对象列表
        List<GradeVO> finalResult = new ArrayList<>(gradeIds.size());
        for (Integer id : gradeIds) {
            finalResult.add(cachedGradesMap.get(id));
        }

        // 构建并返回IPage对象
        IPage<GradeVO> resultPage = new Page<>(pageNum, pageSize, Long.valueOf(total));
        resultPage.setRecords(finalResult);

        return Result.success("查询成功", resultPage);
    }

    @Override
    public Result<String> removeUserGrade(String ids) {
        // 字符串转换为列表
        List<Integer> userIds = Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .toList();
        // 移出班级
        int rows = userMapper.removeUserGrade(userIds);
        if (rows == 0) {
            return Result.failed("移除失败");
        }
        userIds.forEach(id->{
            stringRedisTemplate.delete("cache:grade:getPaging:"+id.toString());
        });

        return Result.success("移除成功");
    }

    @Override
    public Result<List<GradeVO>> getAllGrade() {
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        List<GradeVO> grades = gradeMapper.getAllGrade(userId,roleCode);
        return Result.success("查询成功", grades);
    }

    @Override
    public Result teacherJoinClass(String code) {
        Grade grade = gradeMapper.getGradeById(code);
        Integer userId = SecurityUtil.getUserId();
        UserGrade userGrade = new UserGrade();
        userGrade.setGId(grade.getId());
        userGrade.setUId(userId);
        int insert = userGradeMapper.insert(userGrade);
        if(insert>0){
            return Result.success("加入成功");
        }
        return Result.failed("加入失败");
    }

    @Override
    public Result teacherExitClass(String gradeId) {
        Integer userId = SecurityUtil.getUserId();
        Integer row = userGradeMapper.teacherExitClass(userId,gradeId);
        if(row >0){
            return Result.success("退出成功");
        }
        return Result.failed("退出失败");
    }

    @Override
    public Result userExitGrade() {
        Integer gradeId = SecurityUtil.getGradeId();
        Integer userId = SecurityUtil.getUserId();
        Integer row = userMapper.userExitGrade(gradeId,userId);
        if(row>0){
            return Result.success("退出成功");
        }
        return Result.failed("退出失败");
    }

}

