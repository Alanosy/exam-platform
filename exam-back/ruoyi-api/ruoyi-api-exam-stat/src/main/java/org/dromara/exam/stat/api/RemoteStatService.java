package org.dromara.exam.stat.api;

import org.dromara.exam.stat.api.domain.HomeStatVo;

/**
 * 考试统计服务（跨服务调用）
 *
 * <p>首页和各种看板都从这里取数。统计本身是「读多写少」的事，
 * 但数据来源天然分库：考试在 ry-exam，答卷在 ry-exam-answer，
 * 所以由本服务负责把两头的数拼起来，调用方不必知道数据在哪一边。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
public interface RemoteStatService {

    /**
     * 首页总览
     *
     * <p>答卷服务挂掉不该让首页整个打不开：那几项降级成 0，页面照常显示。
     *
     * @return 总览数据，永远非空
     */
    HomeStatVo homeOverview();

}
