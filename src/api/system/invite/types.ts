export interface InviteVO {
  /**
   * 主键ID
   */
  id: string | number;

  /**
   * 考试ID
   */
  examId: string | number;

  /**
   * 邀请账号：手机号/邮箱
   */
  inviteAccount: string;

  /**
   * sms短信 / email邮件
   */
  inviteType: string;

  /**
   * send已发送 / accept已进入考试 / expire已过期
   */
  inviteStatus: string;

  /**
   * 邀请发送时间
   */
  inviteTime: string;

}

export interface InviteForm extends BaseEntity {
  /**
   * 主键ID
   */
  id?: string | number;

  /**
   * 考试ID
   */
  examId?: string | number;

  /**
   * 邀请账号：手机号/邮箱
   */
  inviteAccount?: string;

  /**
   * sms短信 / email邮件
   */
  inviteType?: string;

  /**
   * send已发送 / accept已进入考试 / expire已过期
   */
  inviteStatus?: string;

  /**
   * 邀请发送时间
   */
  inviteTime?: string;

}

export interface InviteQuery extends PageQuery {

  /**
   * 考试ID
   */
  examId?: string | number;

  /**
   * 邀请账号：手机号/邮箱
   */
  inviteAccount?: string;

  /**
   * sms短信 / email邮件
   */
  inviteType?: string;

  /**
   * send已发送 / accept已进入考试 / expire已过期
   */
  inviteStatus?: string;

  /**
   * 邀请发送时间
   */
  inviteTime?: string;

  /**
   * 日期范围参数
   */
  params?: any;
}



