package org.dromara.exam.proctor.dubbo;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.exam.proctor.api.RemoteProctorService;
import org.dromara.exam.proctor.api.domain.RemoteProctorSessionVo;
import org.dromara.exam.proctor.domain.ProctorSession;
import org.dromara.exam.proctor.mapper.ProctorSessionMapper;
import org.dromara.exam.proctor.service.IProctorService;
import org.springframework.stereotype.Service;

/**
 * 防作弊服务对外实现（答题服务交卷时调用）
 *
 * <p>交卷通知失败也不能影响交卷本身，所以这里把异常吞掉只记 warn：
 * 最坏情况是会话状态停在「作答中」，监考端按心跳超时照样能判掉线。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DubboService
public class RemoteProctorServiceImpl implements RemoteProctorService {

    private final IProctorService proctorService;
    private final ProctorSessionMapper sessionMapper;

    @Override
    public void finishSession(Long recordId, String status) {
        try {
            proctorService.finishSession(recordId, status);
        } catch (Exception e) {
            log.warn("结束监考会话失败 recordId={}, {}", recordId, e.getMessage());
        }
    }

    @Override
    public RemoteProctorSessionVo queryByRecordId(Long recordId) {
        try {
            ProctorSession session = sessionMapper.selectOne(
                Wrappers.lambdaQuery(ProctorSession.class)
                    .eq(ProctorSession::getRecordId, recordId)
            );
            if (session == null) {
                return null;
            }
            return MapstructUtils.convert(session, RemoteProctorSessionVo.class);
        } catch (Exception e) {
            log.warn("查询监考会话失败 recordId={}, {}", recordId, e.getMessage());
            return null;
        }
    }
}
