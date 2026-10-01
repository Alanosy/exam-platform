package org.dromara.exam.mark.dubbo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.exam.mark.api.RemoteMarkService;
import org.dromara.exam.mark.api.domain.RemoteMarkSyncBo;
import org.dromara.exam.mark.service.IMarkService;
import org.springframework.stereotype.Service;

/**
 * 阅卷服务对外实现（答题服务交卷后调用）
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DubboService
public class RemoteMarkServiceImpl implements RemoteMarkService {

    private final IMarkService markService;

    @Override
    public Long syncSubjective(RemoteMarkSyncBo bo) {
        try {
            return markService.syncSubjective(bo);
        } catch (Exception e) {
            // 建阅卷任务失败不能把交卷整条链路带崩，答题服务那边还会记一条 warn
            log.warn("同步阅卷任务失败 {}", e.getMessage());
            return null;
        }
    }
}
