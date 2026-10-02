package org.dromara.exam.stat.dubbo;

import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.exam.stat.api.RemoteStatService;
import org.dromara.exam.stat.api.domain.HomeStatVo;
import org.dromara.exam.stat.service.IStatHomeService;
import org.springframework.stereotype.Service;

/**
 * 考试统计服务对外实现
 *
 * <p>目前只有首页要用，将来别的看板从这里长出来即可，控制器那条链路不用动。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Service
@RequiredArgsConstructor
@DubboService
public class RemoteStatServiceImpl implements RemoteStatService {

    private final IStatHomeService statHomeService;

    @Override
    public HomeStatVo homeOverview() {
        return statHomeService.homeOverview();
    }

}
