package org.dromara.exam.answer.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.exam.answer.service.IExamRecordService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 超时答卷兜底自动交卷
 *
 * <p>考生直接关掉浏览器时不会触发交卷，答卷会一直停在「答题中」。
 * 这个任务定时把已经用完时间（或考试已结束）的答卷收掉，按已作答内容判分。
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamTimeoutTask {

    private final IExamRecordService examRecordService;

    /**
     * 每 5 分钟跑一次，交卷失败的会在下一轮继续尝试
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void autoSubmit() {
        int count = examRecordService.autoSubmitExpired();
        if (count > 0) {
            log.info("超时答卷自动交卷完成，共 {} 份", count);
        }
    }
}
