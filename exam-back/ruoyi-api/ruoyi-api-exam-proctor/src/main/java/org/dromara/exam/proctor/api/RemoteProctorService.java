package org.dromara.exam.proctor.api;

import org.dromara.exam.proctor.api.domain.RemoteProctorSessionVo;

/**
 * 防作弊服务对外 RPC 接口
 *
 * <p>答题服务交卷后通知监考会话结束，避免考生直接关掉浏览器时会话一直挂在「作答中」。
 * 事件上报本身走 HTTP（/proctor/** 经网关），不走 Dubbo：前端高频调用，HTTP 更直观也更好排错。
 *
 * @author ruoyi
 */
public interface RemoteProctorService {

    /**
     * 结束某份答卷的监考会话
     *
     * @param recordId 答卷记录ID
     * @param status   submitted正常交卷 / force_submit违规强制交卷
     */
    void finishSession(Long recordId, String status);

    /**
     * 查询某份答卷的监考会话
     *
     * @param recordId 答卷记录ID
     * @return 会话信息，没有开过监考时返回 null
     */
    RemoteProctorSessionVo queryByRecordId(Long recordId);
}
