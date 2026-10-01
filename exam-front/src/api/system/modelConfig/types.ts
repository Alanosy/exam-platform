export interface ModelConfigVO {
  /**
   * 主键
   */
  id: string | number;

  /**
   * 名称
   */
  configName: string;

  /**
   * 模型类型
   */
  modelType: string;

  /**
   * 模型名称
   */
  modelName: string;

  /**
   * 接口地址
   */
  apiBase: string;

  /**
   * API密钥
   */
  apiKey: string;

  /**
   * 温度
   */
  temperature: number;

  /**
   * 最大输出
   */
  maxTokens: number;

  /**
   * 请求超时时间(ms)
   */
  timeout: number;

  /**
   * 失败重试次数（单个实例重试）
   */
  retryCount: number;

  /**
   * 优先级
   */
  priority: number;

  /**
   * 权重
   */
  weight: number;

  /**
   * 状态
   */
  status: string;

  /**
   * 备注
   */
  remark: string;

}

export interface ModelConfigForm extends BaseEntity {
  /**
   * 主键
   */
  id?: string | number;

  /**
   * 名称
   */
  configName?: string;

  /**
   * 模型类型
   */
  modelType?: string;

  /**
   * 模型名称
   */
  modelName?: string;

  /**
   * 接口地址
   */
  apiBase?: string;

  /**
   * API密钥
   */
  apiKey?: string;

  /**
   * 温度
   */
  temperature?: number;

  /**
   * 最大输出
   */
  maxTokens?: number;

  /**
   * 请求超时时间(ms)
   */
  timeout?: number;

  /**
   * 失败重试次数（单个实例重试）
   */
  retryCount?: number;

  /**
   * 优先级
   */
  priority?: number;

  /**
   * 权重
   */
  weight?: number;

  /**
   * 状态
   */
  status?: string;

  /**
   * 备注
   */
  remark?: string;

}

export interface ModelConfigQuery extends PageQuery {

  /**
   * 名称
   */
  configName?: string;

  /**
   * 模型类型
   */
  modelType?: string;

  /**
   * 模型名称
   */
  modelName?: string;

  /**
   * 接口地址
   */
  apiBase?: string;

  /**
   * 日期范围参数
   */
  params?: any;
}



