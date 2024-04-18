package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.UserExamsScoreMapper;
import cn.org.alan.exam.model.entity.UserExamsScore;
import cn.org.alan.exam.model.vo.score.UserScoreVO;
import cn.org.alan.exam.service.IUserExamsScoreService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
public class UserExamsScoreServiceImpl extends ServiceImpl<UserExamsScoreMapper, UserExamsScore> implements IUserExamsScoreService {
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;

    @Override
    public Result<IPage<UserScoreVO>> pagingScore(Integer pageNum, Integer pageSize, Integer gradeId, Integer examId, String realName) {
        IPage<UserScoreVO> page = new Page<>(pageNum, pageSize);
        page = userExamsScoreMapper.pagingScore(page,gradeId,examId,realName);
        return Result.success(null,page);
    }
}
