package org.dromara.exam.ai.domain.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 中断卡：Agent 缺信息时让人补
 *
 * <p>kind=form 渲染表单，kind=choice 渲染选项，kind=confirm 渲染确认按钮。
 * 前端拿到它就把输入框铺出来，用户填完连同 sessionId 一起回传。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class AiChatAskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** form / choice / confirm */
    private String kind;

    /** 卡片标题 */
    private String title;

    /** 说明正文（Markdown） */
    private String desc;

    /** 需要填写的字段 */
    private List<AiChatFieldVo> fields;

    /** 提交按钮文案 */
    @JsonAlias("submit_text")
    private String submitText;

    /** 取消按钮文案 */
    @JsonAlias("cancel_text")
    private String cancelText;
}
