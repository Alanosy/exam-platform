<template>
  <div>
    <el-dialog v-model="visible" :title="title" width="600px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="小时">
          <el-radio-group v-model="hourValue" @change="hourChange">
            <el-radio-button label="4" value="4" />
            <el-radio-button label="8" value="8" />
            <el-radio-button label="12" value="12" />
            <el-radio-button label="24" value="24" />
            <el-radio-button label="自定义" value="自定义" />
            <el-input-number v-show="hourValue === '自定义'" v-model="customHourValue" :min="1" @change="customHourValueChange"></el-input-number>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="天">
          <el-radio-group v-model="dayValue" @change="dayChange">
            <el-radio-button label="1" value="1" />
            <el-radio-button label="2" value="2" />
            <el-radio-button label="3" value="3" />
            <el-radio-button label="4" value="4" />
            <el-radio-button label="自定义" value="自定义" />
            <el-input-number v-show="dayValue === '自定义'" v-model="customDayValue" :min="1" @change="customDayValueChange"></el-input-number>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="周">
          <el-radio-group v-model="weekValue" @change="weekChange">
            <el-radio-button label="1" value="1" />
            <el-radio-button label="2" value="2" />
            <el-radio-button label="3" value="3" />
            <el-radio-button label="4" value="4" />
            <el-radio-button label="自定义" value="自定义" />
            <el-input-number v-show="weekValue === '自定义'" v-model="customWeekValue" :min="1" @change="customWeekValueChange"></el-input-number>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="月">
          <el-radio-group v-model="monthValue" @change="monthChange">
            <el-radio-button label="1" value="1" />
            <el-radio-button label="2" value="2" />
            <el-radio-button label="3" value="3" />
            <el-radio-button label="4" value="4" />
            <el-radio-button label="自定义" value="自定义" />
            <el-input-number v-show="monthValue === '自定义'" v-model="customMonthValue" :min="1" @change="customMonthValueChange"></el-input-number>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <div>
          <el-button @click="closeDialog">取消</el-button>
          <el-button type="primary" @click="confirm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import useDialog from '@/hooks/useDialog';

interface PropType {
  modelValue?: string;
  data?: string;
}
const prop = withDefaults(defineProps<PropType>(), {
  modelValue: '',
  data: ''
});
const emit = defineEmits(['update:modelValue', 'confirmCallBack']);

const { title, visible, openDialog, closeDialog } = useDialog({
  title: '设置任务到期时间'
});
const formValue = ref();
const valueType = ref();

const hourValue = ref('');
const dayValue = ref('');
const weekValue = ref('');
const monthValue = ref('');

const customHourValue = ref(1);
const customDayValue = ref(1);
const customWeekValue = ref(1);
const customMonthValue = ref(1);

const hourValueConst = ['4', '8', '12', '24'];
const dayAndWeekAndMonthValueConst = ['1', '2', '3', '4'];

const initValue = () => {
  formValue.value = prop.data;
  if (prop.data) {
    const lastStr = prop.data.substring(prop.data.length - 1);
    if (lastStr === 'H') {
      const hourValueValue = prop.data.substring(2, prop.data.length - 1);
      if (hourValueConst.includes(hourValueValue)) {
        hourValue.value = hourValueValue;
      } else {
        hourValue.value = '自定义';
        customHourValue.value = Number(hourValueValue);
      }
    }
    const dayAndWeekAndMonthValue = prop.data.substring(1, prop.data.length - 1);
    if (lastStr === 'D') {
      if (dayAndWeekAndMonthValueConst.includes(dayAndWeekAndMonthValue)) {
        dayValue.value = dayAndWeekAndMonthValue;
      } else {
        dayValue.value = '自定义';
        customDayValue.value = Number(dayAndWeekAndMonthValue);
      }
    }
    if (lastStr === 'W') {
      if (dayAndWeekAndMonthValueConst.includes(dayAndWeekAndMonthValue)) {
        weekValue.value = dayAndWeekAndMonthValue;
      } else {
        weekValue.value = '自定义';
        customWeekValue.value = Number(dayAndWeekAndMonthValue);
      }
    }
    if (lastStr === 'M') {
      if (dayAndWeekAndMonthValueConst.includes(dayAndWeekAndMonthValue)) {
        monthValue.value = dayAndWeekAndMonthValue;
      } else {
        monthValue.value = '自定义';
        customMonthValue.value = Number(dayAndWeekAndMonthValue);
      }
    }
  }
};

const confirm = () => {
  emit('update:modelValue', formValue.value);
  emit('confirmCallBack', formValue.value);
  closeDialog();
};

const customHourValueChange = (customHourValue) => {
  formValue.value = `PT${customHourValue}H`;

  dayValue.value = '';
  weekValue.value = '';
  monthValue.value = '';
  customDayValue.value = 1;
  customWeekValue.value = 1;
  customMonthValue.value = 1;
};
const customDayValueChange = (customDayValue) => {
  formValue.value = `P${customDayValue}D`;
  hourValue.value = '';
  weekValue.value = '';
  monthValue.value = '';

  customHourValue.value = 1;
  customWeekValue.value = 1;
  customMonthValue.value = 1;
};

const customWeekValueChange = (customWeekValue) => {
  formValue.value = `P${customWeekValue}W`;
  hourValue.value = '';
  dayValue.value = '';
  monthValue.value = '';

  customHourValue.value = 1;
  customDayValue.value = 1;
  customMonthValue.value = 1;
};

const customMonthValueChange = (customMonthValue) => {
  formValue.value = `P${customMonthValue}M`;
  hourValue.value = '';
  dayValue.value = '';
  weekValue.value = '';

  customHourValue.value = 1;
  customDayValue.value = 1;
  customWeekValue.value = 1;
};

const hourChange = (hourValue) => {
  if (hourValue === '自定义') {
    formValue.value = `PT${customHourValue.value}H`;
  } else {
    formValue.value = `PT${hourValue}H`;
  }

  dayValue.value = '';
  weekValue.value = '';
  monthValue.value = '';
  customDayValue.value = 1;
  customWeekValue.value = 1;
  customMonthValue.value = 1;
};
const dayChange = (dayValue) => {
  if (dayValue === '自定义') {
    formValue.value = `P${customDayValue.value}D`;
  } else {
    formValue.value = `P${dayValue}D`;
  }

  hourValue.value = '';
  weekValue.value = '';
  monthValue.value = '';

  customHourValue.value = 1;
  customWeekValue.value = 1;
  customMonthValue.value = 1;
};
const weekChange = (weekValue) => {
  if (weekValue === '自定义') {
    formValue.value = `P${customWeekValue.value}W`;
  } else {
    formValue.value = `P${weekValue}W`;
  }

  hourValue.value = '';
  dayValue.value = '';
  monthValue.value = '';

  customHourValue.value = 1;
  customDayValue.value = 1;
  customMonthValue.value = 1;
};
const monthChange = (monthValue) => {
  if (monthValue === '自定义') {
    formValue.value = `P${customMonthValue.value}M`;
  } else {
    formValue.value = `P${monthValue}M`;
  }

  hourValue.value = '';
  dayValue.value = '';
  weekValue.value = '';

  customHourValue.value = 1;
  customDayValue.value = 1;
  customWeekValue.value = 1;
};

watch(
  () => visible.value,
  () => {
    if (visible.value) {
      initValue();
    }
  }
);

defineExpose({
  openDialog,
  closeDialog
});
</script>
