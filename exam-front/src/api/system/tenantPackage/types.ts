export interface TenantPkgVO extends BaseEntity {
  packageId: string | number;
  packageName: string;
  menuIds: string;
  remark: string;
  menuCheckStrictly: boolean;
  status: string;
}

export interface TenantPkgQuery extends PageQuery {
  packageName: string;
}

export interface TenantPkgForm {
  packageId: string | number | undefined;
  packageName: string;
  menuIds: string;
  remark: string;
  menuCheckStrictly: boolean;
}
