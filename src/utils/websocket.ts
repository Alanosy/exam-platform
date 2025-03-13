import { getToken } from '@/utils/auth';
import { ElNotification } from 'element-plus';
import { useNoticeStore } from '@/store/modules/notice';

// 初始化socket
export const initWebSocket = (url: any) => {
  if (import.meta.env.VITE_APP_WEBSOCKET === 'false') {
    return;
  }
  url = url + '?Authorization=Bearer ' + getToken() + '&clientid=' + import.meta.env.VITE_APP_CLIENT_ID;
  useWebSocket(url, {
    autoReconnect: {
      // 重连最大次数
      retries: 3,
      // 重连间隔
      delay: 1000,
      onFailed() {
        console.log('websocket重连失败');
      }
    },
    heartbeat: {
      message: JSON.stringify({ type: 'ping' }),
      // 发送心跳的间隔
      interval: 10000,
      // 接收到心跳response的超时时间
      pongTimeout: 2000
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
