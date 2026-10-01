package org.dromara.exam.practice.dubbo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.exam.practice.api.RemoteWrongQuestionService;
import org.dromara.exam.practice.api.domain.RemoteWrongQuestionBo;
import org.dromara.exam.practice.service.IWrongQuestionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 错题本服务对外实现（供答题服务交卷后、阅卷服务批改后写入错题）
 *
 * @author ruoyi
 * @date 2026-09-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DubboService
public class RemoteWrongQuestionServiceImpl implements RemoteWrongQuestionService {

    private final IWrongQuestionService wrongQuestionService;

    @Override
    public void syncWrong(List<RemoteWrongQuestionBo> boList) {
        wrongQuestionService.syncWrong(boList);
    }

}
