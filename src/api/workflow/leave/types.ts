export interface LeaveVO {
  id: string | number;
  leaveType: string;
  startDate: string;
  endDate: string;
  leaveDays: number;
  remark: string;
  processInstanceVo: any;
}

export interface LeaveForm extends BaseEntity {
  id?: string | number;
  leaveType?: string;
  startDate?: string;
  endDate?: string;
  leaveDays?: number;
  remark?: string;
  processInstanceVo?: any;
}

export interface LeaveQuery extends PageQuery {
  startLeaveDays?: number;
  endLeaveDays?: number;
}
