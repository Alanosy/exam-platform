package org.dromara.exam.mark.service.ai;

/**
 * AI 阅卷能力（预留接入点）
 *
 * <p>阅卷流程对 AI 的依赖只有「给建议分 + 给理由」这一件事，
 * 所以这里把接口收窄成一个方法，接 ruoyi-exam-ai 或任何大模型时
 * 只需要换一个实现类，上层打分逻辑不动。
 *
 * <p>默认实现 {@link DefaultMarkAiServiceImpl} 明确返回「未接入」，
 * 前端按钮依然可用（点了会提示），不会出现调了个寂寞却显示成功的情况。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
import java.util.ArrayList;
import java.util.List;

public interface MarkAiService {

    /**
     * 是否可用
     *
     * @return true 表示已接入，前端才展示 AI 按钮
     */
    boolean enabled();

    /**
     * 评估一道主观题
     *
     * @param bo 判分材料
     * @return 建议分与理由，失败时 success 为 false
     */
    MarkAiResult judge(MarkAiBo bo);

    /**
     * 批量评估主观题
     *
     * <p>默认实现是逐题调 {@link #judge}，串行等待。接了真实模型的实现
     * （RemoteMarkAiServiceImpl）会覆写成一次批量提交——一场考试 200 份卷
     * × 5 道主观题就是 1000 次调用，串行会把教师等到超时。
     *
     * @param bos 判分材料，按此顺序返回结果
     * @return 与入参等长同序的结果，绝不返回 null
     */
    default List<MarkAiResult> judgeBatch(List<MarkAiBo> bos) {
        List<MarkAiResult> out = new ArrayList<>();
        if (bos == null) {
            return out;
        }
        for (MarkAiBo bo : bos) {
            out.add(judge(bo));
        }
        return out;
    }
}
