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

let socketUrl: any = ''; // socket地址
let websocket: any = null; // websocket 实例
let heartTime: any = null; // 心跳定时器实例
let socketHeart = 0 as number; // 心跳次数
const HeartTimeOut = 10000; // 心跳超时时间 10000 = 10s
let socketError = 0 as number; // 错误次数

// 初始化socket
export const initWebSocket = (url: any) => {
  if (import.meta.env.VITE_APP_WEBSOCKET === 'false') {
    return;
  }
  socketUrl = url;
  // 初始化 websocket
  websocket = new WebSocket(url + '?Authorization=Bearer ' + getToken() + '&clientid=' + import.meta.env.VITE_APP_CLIENT_ID);
  websocketonopen();
  websocketonmessage();
  websocketonerror();
  websocketclose();
  sendSocketHeart();
  return websocket;
};

// socket 连接成功
export const websocketonopen = () => {
  websocket.onopen = function () {
    console.log('连接 websocket 成功');
    resetHeart();
  };
};

// socket 连接失败
export const websocketonerror = () => {
  websocket.onerror = function (e: any) {
    console.log('连接 websocket 失败', e);
  };
};

// socket 断开链接
export const websocketclose = () => {
  websocket.onclose = function (e: any) {
    console.log('断开连接', e);
  };
};

// socket 重置心跳
export const resetHeart = () => {
  socketHeart = 0;
  socketError = 0;
  clearInterval(heartTime);
  sendSocketHeart();
};

// socket心跳发送
export const sendSocketHeart = () => {
  heartTime = setInterval(() => {
    // 如果连接正常则发送心跳
    if (websocket.readyState == 1) {
      // if (socketHeart <= 30) {
      websocket.send(
        JSON.stringify({
          type: 'ping'
        })
      );
      socketHeart = socketHeart + 1;
    } else {
      // 重连
      reconnect();
    }
  }, HeartTimeOut);
};

// socket重连
export const reconnect = () => {
  if (socketError <= 2) {
    clearInterval(heartTime);
    initWebSocket(socketUrl);
    socketError = socketError + 1;
    // eslint-disable-next-line prettier/prettier
    console.log('socket重连', socketError);
  } else {
    // eslint-disable-next-line prettier/prettier
    console.log('重试次数已用完');
    clearInterval(heartTime);
  }
};

// socket 发送数据
export const sendMsg = (data: any) => {
  websocket.send(data);
};

// socket 接收数据
export const websocketonmessage = () => {
  websocket.onmessage = function (e: any) {
    if (e.data.indexOf('heartbeat') > 0) {
      resetHeart();
    }
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
    return e.data;
  };
};
