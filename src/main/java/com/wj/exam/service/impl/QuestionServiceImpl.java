package com.wj.exam.service.impl;

import com.wj.exam.entity.Question;
import com.wj.exam.mapper.QuestionMapper;
import com.wj.exam.service.IQuestionService;
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
