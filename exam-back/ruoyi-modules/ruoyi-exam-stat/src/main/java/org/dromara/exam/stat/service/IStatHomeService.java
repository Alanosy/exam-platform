package org.dromara.exam.stat.service;

import org.dromara.exam.stat.api.domain.HomeStatVo;

/**
 * 首页统计
 *
 * @author ruoyi
 * @date 2026-10-02
 */
public interface IStatHomeService {

    /**
     * 首页总览：考试侧的量 + 答卷侧的量
     *
     * @return 总览数据，永远非空
     */
    HomeStatVo homeOverview();

}
