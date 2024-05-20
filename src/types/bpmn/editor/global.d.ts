import { MessageApiInjection } from 'naive-ui/lib/message/src/MessageProvider';

declare global {
  interface Window {
    bpmnInstances: any;
    __messageBox: MessageApiInjection;
    URL: any;
  }
}

declare interface Window {
  bpmnInstances: any;
}
