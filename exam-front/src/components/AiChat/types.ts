import type { AiChatAskVO, AiChatTraceVO } from '@/api/system/ai/types';

/**
 * 聊天窗里的一条消息
 *
 * 为什么 assistant 消息要带 trace 和 ask：
 * 一条助手消息不一定只是「一段话」——它可能是一次带工具调用的运行过程，
 * 也可能是一张等着用户填的中断卡。这三样东西必须绑在同一条消息上，
 * 否则界面上会出现「卡片飘在别的气泡下面」的错乱。
 */
export interface ChatMessageItem {
  id: number;
  role: 'user' | 'assistant';
  content: string;
  trace?: AiChatTraceVO[];
  ask?: AiChatAskVO | null;
  /** 中断卡已提交，卡片置灰不可再点 */
  answered?: boolean;
  loading?: boolean;
  error?: string;
}
