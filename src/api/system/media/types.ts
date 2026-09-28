export interface MediaVO {
  /**
   * 主键ID
   */
  id: string | number;

  /**
   * 试题ID
   */
  questionId: string | number;

  /**
   * media_type:image图片,audio音频,video视频
   */
  mediaType: string;

  /**
   * 资源访问地址MinIO
   */
  mediaUrl: string;

  /**
   * 原始文件名
   */
  mediaName: string;

  /**
   * 展示顺序
   */
  sort: number;

}

export interface MediaForm extends BaseEntity {
  /**
   * 主键ID
   */
  id?: string | number;

  /**
   * 试题ID
   */
  questionId?: string | number;

  /**
   * media_type:image图片,audio音频,video视频
   */
  mediaType?: string;

  /**
   * 资源访问地址MinIO
   */
  mediaUrl?: string;

  /**
   * 原始文件名
   */
  mediaName?: string;

  /**
   * 展示顺序
   */
  sort?: number;

}

export interface MediaQuery extends PageQuery {

  /**
   * 试题ID
   */
  questionId?: string | number;

  /**
   * media_type:image图片,audio音频,video视频
   */
  mediaType?: string;

  /**
   * 资源访问地址MinIO
   */
  mediaUrl?: string;

  /**
   * 原始文件名
   */
  mediaName?: string;

  /**
   * 展示顺序
   */
  sort?: number;

  /**
   * 日期范围参数
   */
  params?: any;
}



