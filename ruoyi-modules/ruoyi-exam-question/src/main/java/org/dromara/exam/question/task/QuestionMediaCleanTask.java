package org.dromara.exam.question.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.exam.question.service.IQuestionMediaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 富文本孤儿附件清理任务
 *
 * <p>富文本里插入图片时文件已经进了对象存储，如果此时用户放弃了编辑（或后续把图片删掉），
 * 这些文件就再也不会被任何人引用。这里按保留时长定期回收：
 * <ol>
 *     <li>找出 question_id 为空且超过保留时长仍未归属试题的 question_media 记录</li>
 *     <li>删除 question_media 记录，并调用文件服务删除存储桶里的对象与 sys_oss 记录</li>
 * </ol>
 *
 * <p>默认每天凌晨 4 点执行，可通过 {@code exam.media.clean-cron} 调整，
 * 保留时长通过 {@code exam.media.retain-hours} 调整（单位小时，默认 24）。
 * 也可以手动调用 {@code POST /question/media/cleanUnused} 立即执行一次。
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "exam.media.clean", name = "enabled", havingValue = "true", matchIfMissing = true)
public class QuestionMediaCleanTask {

    private final IQuestionMediaService questionMediaService;

    /**
     * 孤儿附件保留时长（小时）
     */
    @Value("${exam.media.retain-hours:24}")
    private int retainHours;

    @Scheduled(cron = "${exam.media.clean-cron:0 0 4 * * ?}")
    public void cleanUnusedMedia() {
        try {
            int count = questionMediaService.cleanUnused(retainHours);
            if (count > 0) {
                log.info("清理未使用的试题多媒体附件完成，共 {} 条", count);
            }
        } catch (Exception e) {
            log.error("清理未使用的试题多媒体附件失败", e);
        }
    }
}
