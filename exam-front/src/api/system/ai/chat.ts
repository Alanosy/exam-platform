import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import type { AiChatForm, AiChatVO } from '@/api/system/ai/types';

/**
 * 对话式 AI 助手：跑一轮对话
 *
 * 两种用法：
 * 1. 正常提问：只传 message（首轮不带 sessionId）
 * 2. 回答中断卡：带 sessionId + answers，message 可以为空
 */
export const aiChat = (data: AiChatForm): AxiosPromise<AiChatVO> => {
  return request({
    url: '/ai/chat',
    method: 'post',
    data: data
  });
};
