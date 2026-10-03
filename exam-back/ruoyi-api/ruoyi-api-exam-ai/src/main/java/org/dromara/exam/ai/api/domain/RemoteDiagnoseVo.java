package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 错题归因结论
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteDiagnoseVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否诊断成功 */
    private Boolean success;

    /** 薄弱知识点 */
    private List<RemoteAiWeakPointVo> weakPoints;

    /** 错误类型分布 {"concept":5,"careless":2} */
    private String errorDistribution;

    /** 针对性建议 */
    private String advice;

    /** 优先补的知识点 */
    private List<String> priority;

    /** 使用的模型 */
    private String model;

    /** 失败原因 */
    private String message;

    public static RemoteDiagnoseVo fail(String message) {
        RemoteDiagnoseVo vo = new RemoteDiagnoseVo();
        vo.setSuccess(false);
        vo.setMessage(message);
        return vo;
    }
}
