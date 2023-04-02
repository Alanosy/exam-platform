export interface PostVO extends BaseEntity {
  postId: number | string;
  postCode: string;
  postName: string;
  postSort: number;
  status: string;
  remark: string;
}

export interface PostForm {
  postId: number | string | undefined;
  postCode: string;
  postName: string;
  postSort: number;
  status: string;
  remark: string;
}

export interface PostQuery extends PageQuery {
  postCode: string;
  postName: string;
  status: string;
}
