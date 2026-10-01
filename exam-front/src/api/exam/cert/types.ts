/** 证书模板 */
export interface CertificateVO {
  id?: string;
  /** 证书名称（管理用） */
  certName?: string;
  /** 证书编码 */
  certCode?: string;
  /** 证书大标题 */
  title?: string;
  /** 证书副标题 */
  subtitle?: string;
  /** 正文模板，支持占位符 */
  content?: string;
  /** 发证机构 */
  issuer?: string;
  sealOssId?: string;
  bgOssId?: string;
  bgColor?: string;
  /** 版式 0横版 1竖版 */
  orientation?: string;
  /** 有效期 0永久 1按天 */
  validType?: string;
  validDays?: number;
  /** 已颁发数量 */
  issueCount?: number;
  /** 0启用 1停用 */
  status?: string;
  remark?: string;
  /** 印章访问地址（后端回填） */
  sealUrl?: string;
  /** 背景访问地址（后端回填） */
  bgUrl?: string;
  createTime?: string;
}

/** 证书模板 新增/编辑入参 */
export interface CertificateBO {
  id?: string;
  certName?: string;
  certCode?: string;
  title?: string;
  subtitle?: string;
  content?: string;
  issuer?: string;
  sealOssId?: string;
  bgOssId?: string;
  bgColor?: string;
  orientation?: string;
  validType?: string;
  validDays?: number;
  status?: string;
  remark?: string;
}

/** 证书模板下拉选项 */
export interface CertificateOptionVO {
  id?: string;
  certName?: string;
  certCode?: string;
  title?: string;
  validType?: string;
  validDays?: number;
}

/** 证书颁发记录 */
export interface CertificateRecordVO {
  id?: string;
  certId?: string;
  certName?: string;
  certNo?: string;
  examId?: string;
  examName?: string;
  recordId?: string;
  userId?: string;
  account?: string;
  nickName?: string;
  attemptNo?: number;
  score?: number;
  passScore?: number;
  totalScore?: number;
  title?: string;
  subtitle?: string;
  content?: string;
  issuer?: string;
  sealOssId?: string;
  bgOssId?: string;
  bgColor?: string;
  orientation?: string;
  /** 0自动颁发 1手动补发 */
  issueType?: string;
  issueTime?: string;
  expireTime?: string;
  /** 0有效 1已吊销 */
  status?: string;
  revokeReason?: string;
  revokeTime?: string;
  remark?: string;
  sealUrl?: string;
  bgUrl?: string;
}

/** 证书模板查询条件 */
export interface CertificateQuery extends Record<string, unknown> {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: string;
}

/** 证书颁发记录查询条件 */
export interface CertificateRecordQuery extends Record<string, unknown> {
  pageNum?: number;
  pageSize?: number;
  certId?: string;
  examId?: string;
  keyword?: string;
  status?: string;
}

/** 手工补发入参 */
export interface CertIssueBO {
  examId?: string;
  recordId?: string;
  remark?: string;
}
