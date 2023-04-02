import axios from 'axios';

declare module 'axios' {
  export interface AxiosResponse<T = any> {
    code: number;
    msg: string;
    rows: T;
    total: number;
  }
}
