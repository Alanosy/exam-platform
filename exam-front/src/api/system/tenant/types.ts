export interface TenantVO extends BaseEntity {
  id: number | string;
  tenantId: number | string;
  username: string;
  contactUserName: string;
  contactPhone: string;
  companyName: string;
  licenseNumber: string;
  address: string;
  domain: string;
  intro: string;
  remark: string;
  packageId: string | number;
  expireTime: string;
  accountCount: number;
  status: string;
}

export interface TenantQuery extends PageQuery {
  tenantId: string | number;

  contactUserName: string;

  contactPhone: string;

  companyName: string;
}

export interface TenantForm {
  id: number | string | undefined;
  tenantId: number | string | undefined;
  username: string;
  password: string;
  contactUserName: string;
  contactPhone: string;
  companyName: string;
  licenseNumber: string;
  domain: string;
  address: string;
  intro: string;
  remark: string;
  packageId: string | number;
  expireTime: string;
  accountCount: number;
  status: string;
}
