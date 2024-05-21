<template>
  <div>
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
      <el-collapse v-model="currentCollapseItem">
        <el-collapse-item name="1">
          <template #title>
            <div class="collapse__title">
              <el-icon>
                <InfoFilled />
              </el-icon>
              常规
            </div>
          </template>
          <div>
            <el-form-item prop="id" label="节点 ID">
              <el-input v-model="formData.id" @change="idChange"> </el-input>
            </el-form-item>
            <el-form-item prop="name" label="节点名称">
              <el-input v-model="formData.name" @change="nameChange"> </el-input>
            </el-form-item>
          </div>
        </el-collapse-item>

        <el-collapse-item name="2">
          <template #title>
            <div class="collapse__title">
              <el-icon>
                <BellFilled />
              </el-icon>
              执行监听器
            </div>
          </template>
          <div>
            <ExecutionListener :element="element"></ExecutionListener>
          </div>
        </el-collapse-item>
        <el-collapse-item name="3">
          <template #title>
            <div class="collapse__title">
              <el-icon>
                <HelpFilled />
              </el-icon>
              多实例
            </div>
          </template>
          <div>
            <el-form-item label="多实例类型">
              <el-select v-model="formData.multiInstanceType" @change="multiInstanceTypeChange">
                <el-option v-for="item in constant.MultiInstanceType" :key="item.id" :value="item.value" :label="item.label"> </el-option>
              </el-select>
            </el-form-item>

            <div v-if="formData.multiInstanceType !== MultiInstanceTypeEnum.NONE">
              <el-form-item label="集合">
                <template #label>
                  <span>
                    集合
                    <el-tooltip placement="top">
                      <el-icon><QuestionFilled /></el-icon>
                      <template #content>
                        属性会作为表达式进行解析。如果表达式解析为字符串而不是一个集合，<br />
                        不论是因为本身配置的就是静态字符串值，还是表达式计算结果为字符串，<br />
                        这个字符串都会被当做变量名，并从流程变量中用于获取实际的集合。
                      </template>
                    </el-tooltip>
                  </span>
                </template>
                <el-input v-model="formData.collection" @change="collectionChange"></el-input>
              </el-form-item>
              <el-form-item label="元素变量">
                <template #label>
                  <span>
                    元素变量
                    <el-tooltip placement="top">
                      <el-icon><QuestionFilled /></el-icon>
                      <template #content>
                        每创建一个用户任务前，先以该元素变量为label，集合中的一项为value，<br />
                        创建（局部）流程变量，该局部流程变量被用于指派用户任务。<br />
                        一般来说，该字符串应与指定人员变量相同。
                      </template>
                    </el-tooltip>
                  </span>
                </template>
                <el-input v-model="formData.elementVariable" @change="elementVariableChange"> </el-input>
              </el-form-item>
              <el-form-item label="完成条件">
                <template #label>
                  <span>
                    完成条件
                    <el-tooltip placement="top">
                      <el-icon><QuestionFilled /></el-icon>
                      <template #content>
                        多实例活动在所有实例都完成时结束，然而也可以指定一个表达式，在每个实例<br />
                        结束时进行计算。当表达式计算为true时，将销毁所有剩余的实例，并结束多实例<br />
                        活动，继续执行流程。例如 ${nrOfCompletedInstances/nrOfInstances >= 0.6 }，<br />
                        表示当任务完成60%时，该节点就算完成
                      </template>
                    </el-tooltip>
                  </span>
                </template>
                <el-input v-model="formData.completionCondition" @change="completionConditionChange"> </el-input>
              </el-form-item>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-form>
  </div>
</template>
<script setup lang="ts">
import ExecutionListener from './property/ExecutionListener.vue';
import useParseElement from '../hooks/useParseElement';
import usePanel from '../hooks/usePanel';
import type { ModdleElement } from 'bpmn';
import type { SubProcessPanel } from 'bpmnDesign';
import { MultiInstanceTypeEnum } from '@/enums/bpmn/IndexEnums';

interface PropType {
  element: ModdleElement;
}
const props = withDefaults(defineProps<PropType>(), {});
const { nameChange, idChange, updateProperties, createModdleElement, constant } = usePanel({
  element: toRaw(props.element)
});
const { parseData } = useParseElement({
  element: toRaw(props.element)
});

const formData = ref(parseData<SubProcessPanel>());
const currentCollapseItem = ref(['1', '2', '3']);

const multiInstanceTypeChange = (newVal) => {
  if (newVal !== MultiInstanceTypeEnum.NONE) {
    let loopCharacteristics = props.element.businessObject.get('loopCharacteristics');
    if (!loopCharacteristics) {
      loopCharacteristics = createModdleElement('bpmn:MultiInstanceLoopCharacteristics', {}, props.element.businessObject);
    }
    loopCharacteristics.isSequential = newVal === MultiInstanceTypeEnum.SERIAL;
    updateProperties({ loopCharacteristics: loopCharacteristics });
  } else {
    updateProperties({ loopCharacteristics: undefined });
  }
};
const collectionChange = (newVal) => {
  let loopCharacteristics = props.element.businessObject.get('loopCharacteristics');
  if (!loopCharacteristics) {
    loopCharacteristics = createModdleElement('bpmn:MultiInstanceLoopCharacteristics', {}, props.element.businessObject);
  }
  loopCharacteristics.collection = newVal && newVal.length > 0 ? newVal : undefined;
  updateProperties({ loopCharacteristics: loopCharacteristics });
};
const elementVariableChange = (newVal) => {
  let loopCharacteristics = props.element.businessObject.get('loopCharacteristics');
  if (!loopCharacteristics) {
    loopCharacteristics = createModdleElement('bpmn:MultiInstanceLoopCharacteristics', {}, props.element.businessObject);
  }
  loopCharacteristics.elementVariable = newVal && newVal.length > 0 ? newVal : undefined;
  updateProperties({ loopCharacteristics: loopCharacteristics });
};
const completionConditionChange = (newVal) => {
  let loopCharacteristics = props.element.businessObject.get<ModdleElement>('loopCharacteristics');
  if (!loopCharacteristics) {
    loopCharacteristics = createModdleElement('bpmn:MultiInstanceLoopCharacteristics', {}, props.element.businessObject);
  }
  if (newVal && newVal.length > 0) {
    if (!loopCharacteristics.completionCondition) {
      loopCharacteristics.completionCondition = createModdleElement('bpmn:Expression', { body: newVal }, loopCharacteristics);
    } else {
      loopCharacteristics.completionCondition.body = newVal;
    }
  } else {
    loopCharacteristics.completionCondition = undefined;
  }
  updateProperties({ loopCharacteristics: loopCharacteristics });
};

onBeforeMount(() => {
  if (formData.value.loopCharacteristics) {
    const loopCharacteristics = formData.value.loopCharacteristics;
    formData.value.collection = loopCharacteristics.collection || '';
    formData.value.elementVariable = loopCharacteristics.elementVariable || '';
    formData.value.completionCondition = loopCharacteristics.completionCondition?.body || '';
    formData.value.multiInstanceType = loopCharacteristics.isSequential ? MultiInstanceTypeEnum.SERIAL : MultiInstanceTypeEnum.PARALLEL;
  }
});

const formRules = ref<ElFormRules>({
  id: [{ required: true, message: '请输入', trigger: 'blur' }],
  name: [{ required: true, message: '请输入', trigger: 'blur' }]
});
</script>

<style lang="scss" scoped></style>
