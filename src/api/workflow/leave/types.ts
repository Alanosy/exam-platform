export interface LeaveVO {
  id: string | number;
  leaveType: string;
  startDate: string;
  endDate: string;
  leaveDays: number;
  remark: string;
  status?: string;
}

export interface LeaveForm extends BaseEntity {
  id?: string | number;
  leaveType?: string;
  startDate?: string;
  endDate?: string;
  leaveDays?: number;
  remark?: string;
  status?: string;
}

export interface LeaveQuery extends PageQuery {
  startLeaveDays?: number;
  endLeaveDays?: number;
}
