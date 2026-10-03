import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import type { AiChatForm, AiChatIdentityVO, AiChatSessionVO, AiChatVO } from '@/api/system/ai/types';

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

/**
 * 历史会话列表（服务端按当前用户隔离，只看得到自己的）
 */
export const aiChatSessions = (): AxiosPromise<AiChatSessionVO[]> => {
  return request({
    url: '/ai/chat/sessions',
    method: 'get'
  });
};

/**
 * 会话详情：点历史会话「接着聊」时用它把上下文拉回来
 */
export const aiChatSession = (sessionId: string): AxiosPromise<AiChatSessionVO> => {
  return request({
    url: `/ai/chat/session/${sessionId}`,
    method: 'get'
  });
};

/**
 * 我在 AI 眼里是谁：角色归类 + 可见能力数（设置面板展示用）
 */
export const aiChatWhoami = (): AxiosPromise<AiChatIdentityVO> => {
  return request({
    url: '/ai/chat/whoami',
    method: 'get'
  });
};
