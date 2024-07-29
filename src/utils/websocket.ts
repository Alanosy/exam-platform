/**
 * @module initWebSocket 初始化
 * @module websocketonopen 连接成功
 * @module websocketonerror 连接失败
 * @module websocketclose 断开连接
 * @module resetHeart 重置心跳
 * @module sendSocketHeart 心跳发送
 * @module reconnect 重连
 * @module sendMsg 发送数据
 * @module websocketonmessage 接收数据
 * @module test 测试收到消息传递
 * @description socket 通信
 * @param {any} url socket地址
 * @param {any} websocket websocket 实例
 * @param {any} heartTime 心跳定时器实例
 * @param {number} socketHeart 心跳次数
 * @param {number} HeartTimeOut 心跳超时时间
 * @param {number} socketError 错误次数
 */

import { getToken } from '@/utils/auth';
import { ElNotification } from 'element-plus';
import useNoticeStore from '@/store/modules/notice';

// 初始化socket
export const initWebSocket = (url: any) => {
  if (import.meta.env.VITE_APP_WEBSOCKET === 'false') {
    return;
  }
  url = url + '?Authorization=Bearer ' + getToken() + '&clientid=' + import.meta.env.VITE_APP_CLIENT_ID
  useWebSocket(url, {
    autoReconnect: {
      // 重连最大次数
      retries: 3,
      // 重连间隔
      delay: 1000,
      onFailed() {
        console.log('websocket重连失败');
      },
    },
    heartbeat: {
      message: JSON.stringify({type: 'ping'}),
      // 发送心跳的间隔
      interval: 10000,
      // 接收到心跳response的超时时间
      pongTimeout: 2000,
    },
    onConnected() {
      console.log('websocket已经连接');
    },
    onDisconnected() {
      console.log('websocket已经断开');
    },
    onMessage: (_, e) => {
      if (e.data.indexOf('ping') > 0) {
        return;
      }
      useNoticeStore().addNotice({
        message: e.data,
        read: false,
        time: new Date().toLocaleString()
      });
      ElNotification({
        title: '消息',
        message: e.data,
        type: 'success',
        duration: 3000
      });
    }
  });
};
