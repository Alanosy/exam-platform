/**
 * AI 服务对外 RPC 接口
 * <p>
 * 对外提供 AI 出题、AI 自动阅卷、AI 错题推荐、AI 试卷分析等能力。
 * 实现位于 ruoyi-exam-ai 业务模块，内部通过 HTTP REST 转发至
 * Python FastAPI Agent 服务（ruoyi-exam-agent，端口 9221）。
 *
 * @author ruoyi
 */
package org.dromara.exam.ai.api;
