package org.dromara.exam.stat.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.exam.stat.domain.ref.ExamRef;
import org.dromara.exam.stat.mapper.ExamRefMapper;
import org.dromara.exam.stat.service.IStatExamService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 统计定时重算
 *
 * <p>为什么还需要定时：纯靠交卷/阅卷事件做增量，遇到并发改分、服务重启丢事件
 * 就会漂移，而且漂了没有自愈的机会。定时任务每隔一段时间把「近期有考试在跑」
 * 的场次全量重算一遍，把增量算错的地方刷回来。
 *
 * <p>只扫「进行中」和「最近 3 天结束」的考试：已经归档的老考试分数不会再变，
 * 没必要每天跟着重算一遍。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatCalcScheduler {

    private final ExamRefMapper examRefMapper;
    private final IStatExamService statExamService;

    /**
     * 每 10 分钟跑一次
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void recalcRecent() {
        Date since = DateUtil.offsetDay(new Date(), -3);
        LambdaQueryWrapper<ExamRef> lqw = Wrappers.lambdaQuery();
        lqw.and(w -> w.eq(ExamRef::getStatus, "ongoing")
                .or().eq(ExamRef::getStatus, "not_start")
                .or().ge(ExamRef::getEndTime, since))
            .orderByDesc(ExamRef::getId);
        List<ExamRef> exams = examRefMapper.selectList(lqw.last("limit 50"));
        if (CollUtil.isEmpty(exams)) {
            return;
        }
        for (ExamRef exam : exams) {
            statExamService.recalc(exam.getId(), "job");
        }
        log.info("统计定时重算完成，共 {} 场考试", exams.size());
    }
}
