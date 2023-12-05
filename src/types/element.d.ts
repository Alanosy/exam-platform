import type * as ep from 'element-plus';
declare global {
  declare type ElTagType = '' | 'success' | 'warning' | 'info' | 'danger' | 'default' | 'primary';
  declare type ElFormInstance = InstanceType<typeof ep.ElForm>;
  declare type ElTableInstance = InstanceType<typeof ep.ElTable>;
  declare type ElTreeInstance = InstanceType<typeof ep.ElTree>;
  declare type ElTreeSelectInstance = InstanceType<typeof ep.ElTreeSelect>;
  declare type ElSelectInstance = InstanceType<typeof ep.ElSelect>;
  declare type ElUploadInstance = InstanceType<typeof ep.ElUpload>;
  declare type ElCardInstance = InstanceType<typeof ep.ElCard>;
  declare type ElDialogInstance = InstanceType<typeof ep.ElDialog>;
  declare type ElInputInstance = InstanceType<typeof ep.ElInput>;
  declare type ElInputNumberInstance = InstanceType<typeof ep.ElInputNumber>;
  declare type ElRadioInstance = InstanceType<typeof ep.ElRadio>;
  declare type ElRadioGroupInstance = InstanceType<typeof ep.ElRadioGroup>;
  declare type ElRadioButtonInstance = InstanceType<typeof ep.ElRadioButton>;
  declare type ElCheckboxInstance = InstanceType<typeof ep.ElCheckbox>;
  declare type ElCheckboxGroupInstance = InstanceType<typeof ep.ElCheckboxGroup>;
  declare type ElSwitchInstance = InstanceType<typeof ep.ElSwitch>;
  declare type ElDatePickerInstance = InstanceType<typeof ep.ElDatePicker>;
  declare type ElTimePickerInstance = InstanceType<typeof ep.ElTimePicker>;
  declare type ElTimeSelectInstance = InstanceType<typeof ep.ElTimeSelect>;
  declare type ElCascaderInstance = InstanceType<typeof ep.ElCascader>;
  declare type ElColorPickerInstance = InstanceType<typeof ep.ElColorPicker>;
  declare type ElRateInstance = InstanceType<typeof ep.ElRate>;
  declare type ElSliderInstance = InstanceType<typeof ep.ElSlider>;
  declare type ElScrollbarInstance = InstanceType<typeof ep.ElScrollbar>;

  declare type TransferKey = ep.TransferKey;
  declare type CheckboxValueType = ep.CheckboxValueType;
  declare type ElFormRules = ep.FormRules;
  declare type DateModelType = ep.DateModelType;
  declare type UploadFile = typeof ep.UploadFile;
}
