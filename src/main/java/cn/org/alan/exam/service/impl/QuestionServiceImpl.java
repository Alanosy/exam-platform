package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.service.IQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {

}
