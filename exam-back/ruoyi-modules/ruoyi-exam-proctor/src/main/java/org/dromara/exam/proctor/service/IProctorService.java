package org.dromara.exam.proctor.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.exam.proctor.domain.bo.ProctorEventBo;
import org.dromara.exam.proctor.domain.bo.ProctorEventQueryBo;
import org.dromara.exam.proctor.domain.bo.ProctorSessionBo;
import org.dromara.exam.proctor.domain.bo.ProctorStartBo;
import org.dromara.exam.proctor.domain.vo.ProctorEventVo;
import org.dromara.exam.proctor.domain.vo.ProctorOverviewVo;
import org.dromara.exam.proctor.domain.vo.ProctorReportVo;
import org.dromara.exam.proctor.domain.vo.ProctorSessionVo;
import org.dromara.exam.proctor.domain.vo.ProctorSnapshotVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 防作弊服务
 *
 * @author ruoyi
 * @date 2026-10-01
 */
public interface IProctorService {

    /**
     * 进入答题页：开启（或续上）监考会话
     *
     * @param bo 考试与答卷信息
     * @return 会话及本场考试生效的防作弊规则，前端据此决定采集哪些行为
     */
    ProctorSessionVo startSession(ProctorStartBo bo);

    /**
     * 批量上报防作弊事件
     *
     * @param sessionId 会话ID
     * @param events    事件列表
     * @return 当前计数与是否已达到强制交卷条件
     */
    ProctorReportVo report(Long sessionId, List<ProctorEventBo> events);

    /**
     * 心跳：只刷新最近活跃时间，用来判断考生还在不在
     *
     * @param sessionId 会话ID
     */
    void heartbeat(Long sessionId);

    /**
     * 结束会话（交卷 / 强制交卷）
     *
     * @param recordId 答卷记录ID
     * @param status   submitted正常交卷 / force_submit强制交卷
     */
    void finishSession(Long recordId, String status);

    /**
     * 监考会话分页列表（发布者视角，只能看自己创建的考试）
     */
    TableDataInfo<ProctorSessionVo> listSessionPage(ProctorSessionBo bo, PageQuery pageQuery);

    /**
     * 会话详情
     */
    ProctorSessionVo getSession(Long sessionId);

    /**
     * 事件流水分页
     */
    TableDataInfo<ProctorEventVo> listEventPage(ProctorEventQueryBo bo, PageQuery pageQuery);

    /**
     * 摄像头抓拍列表（倒序，最多 200 张）
     */
    List<ProctorSnapshotVo> listSnapshots(Long sessionId);

    /**
     * 保存一次摄像头抓拍
     *
     * @param sessionId 会话ID
     * @param eventType 触发场景 periodic / enter / switch_screen / resume
     * @param file      抓拍图片
     */
    ProctorSnapshotVo saveSnapshot(Long sessionId, String eventType, MultipartFile file);

    /**
     * 某场考试的监考概览
     */
    ProctorOverviewVo overview(Long examId);
}
