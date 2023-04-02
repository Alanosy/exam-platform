<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div class="search" v-show="showSearch">
        <el-form :model="queryParams" ref="qeuryFormRef" :inline="true" label-width="68px">
          <el-form-item label="树节点名" prop="treeName">
            <el-input v-model="queryParams.treeName" placeholder="请输入树节点名" clearable style="width: 200px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="daterangeCreateTime"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="daterange"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </transition>

    <el-card shadow="never">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd()" v-hasPermi="['demo:tree:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="info" plain icon="Sort" @click="handleToggleExpandAll">展开/折叠</el-button>
          </el-col>
          <right-toolbar :columns="columns" v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table
        v-if="refreshTable"
        v-loading="loading"
        :data="treeList"
        row-key="id"
        :default-expand-all="isExpandAll"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        ref="demoTreeTableRef"
      >
        <el-table-column label="父id" prop="parentId" v-if="columns[0].visible" />
        <el-table-column label="部门id" align="center" prop="deptId" v-if="columns[1].visible" />
        <el-table-column label="用户id" align="center" prop="userId" v-if="columns[2].visible" />
        <el-table-column label="树节点名" align="center" prop="treeName" v-if="columns[3].visible" />
        <el-table-column label="创建时间" align="center" prop="createTime" v-if="columns[4].visible" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" align="center" width="150" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="修改" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['demo:tree:edit']"></el-button>
            </el-tooltip>
            <el-tooltip content="新增" placement="top">
              <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['demo:tree:add']"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['demo:tree:remove']"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加或修改测试树表对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px" append-to-body>
      <el-form ref="treeRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="父id" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="treeOptions"
            :props="{ value: 'id', label: 'treeName', children: 'children' }"
            value-key="id"
            check-strictly
            placeholder="请选择父id"
          />
        </el-form-item>
        <el-form-item label="部门id" prop="deptId">
          <el-input v-model="form.deptId" placeholder="请输入部门id" />
        </el-form-item>
        <el-form-item label="用户id" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户id" />
        </el-form-item>
        <el-form-item label="树节点名" prop="treeName">
          <el-input v-model="form.treeName" placeholder="请输入树节点名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Tree" lang="ts">
import { listTree, getTree, delTree, addTree, updateTree } from '@/api/demo/tree';
import { DemoTreeVO, DemoTreeForm, DemoTreeOptionsType, DemoTreeQuery } from '@/api/demo/types';
import { ComponentInternalInstance } from 'vue';
import { ElTree, ElForm, ElTable, DateModelType } from 'element-plus';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const treeList = ref<DemoTreeVO[]>([]);
const buttonLoading = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const isExpandAll = ref(true);
const refreshTable = ref(true);
const treeOptions = ref<DemoTreeOptionsType[]>([]);
const daterangeCreateTime = ref<[DateModelType, DateModelType]>(['', '']);

const treeRef = ref(ElTree);
const qeuryFormRef = ref(ElForm);
const demoTreeTableRef = ref(ElTable)

const dialog = reactive<DialogOption>({
    visible: false,
    title: ''
});

// 列显隐信息
const columns = ref<FieldOption[]>([
    { key: 0, label: `父id`, visible: false },
    { key: 1, label: `部门id`, visible: true },
    { key: 2, label: `用户id`, visible: true },
    { key: 3, label: `树节点名`, visible: true },
    { key: 4, label: `创建时间`, visible: true }
]);

const initFormData = {
    id: undefined,
    parentId: undefined,
    deptId: undefined,
    userId: undefined,
    treeName: ''
}

const data = reactive<PageData<DemoTreeForm, DemoTreeQuery>>({
    // 查询参数
    queryParams: {
        treeName: '',
        createTime: '',
    },
    // 表单参数
    form: {...initFormData},
    // 表单校验
    rules: {
        treeName: [{ required: true, message: "树节点名不能为空", trigger: "blur" }],
    }
});

const { queryParams, form, rules } = toRefs(data);
/** 查询测试树表列表 */
const getList = () => {
    loading.value = true;
    listTree(proxy?.addDateRange(queryParams.value, daterangeCreateTime.value, "CreateTime")).then(res => {
        const data = proxy?.handleTree<DemoTreeVO>(res.data, "id", "parentId");
        if (data) {
            treeList.value = data
        }
        loading.value = false;
    });
}

/** 查询部门下拉树结构 */
const getTreeSelect = async () => {
    listTree(proxy?.addDateRange(queryParams.value, daterangeCreateTime.value, "CreateTime")).then(res => {
        const topData: DemoTreeOptionsType = { id: 0, treeName: '顶级节点', children: [] };
        topData.children = proxy?.handleTree<DemoTreeOptionsType>(res.data, "id", "parentId");
        treeOptions.value.push(topData);
    });
}
/** 取消按钮 */
const cancel = () => {
    reset();
    dialog.visible = false;
}
/** 表单重置 */
const reset = () => {
    form.value = {...initFormData}
    treeRef.value.resetFields();
}
/** 搜索按钮操作 */
const handleQuery = () => {
    getList();
}
/** 重置按钮操作 */
const resetQuery = () => {
    daterangeCreateTime.value = ['', ''];
    qeuryFormRef.value.resetFields();
    handleQuery();
}
/** 新增按钮操作 */
const handleAdd = (row?: DemoTreeVO) => {
    dialog.visible = true;
    dialog.title = "添加测试树表";
    nextTick(() => {
        reset();
        getTreeSelect();
        if (row != null && row.id) {
            form.value.parentId = row.id;
        } else {
            form.value.parentId = 0;
        }
    })
}

/** 展开/折叠操作 */
const handleToggleExpandAll = () => {
    isExpandAll.value = !isExpandAll.value;
    toggleExpandAll(treeList.value, isExpandAll.value)
}
/** 展开/折叠所有 */
const toggleExpandAll = (data: DemoTreeVO[], status: boolean) => {
    data.forEach((item) => {
        demoTreeTableRef.value.toggleRowExpansion(item, status)
        if(item.children && item.children.length > 0) toggleExpandAll(item.children, status)
    })
}

/** 修改按钮操作 */
const handleUpdate = async (row: DemoTreeVO) => {
    loading.value = true;
    dialog.visible = true;
    dialog.title = "修改测试树表";
    nextTick(async () => {
        reset();
        getTreeSelect();
        if (row) {
            form.value.parentId = row.id;
        }
        getTree(row.id).then((response) => {
            loading.value = false;
            form.value = response.data;

        });
    })
}
/** 提交按钮 */
const submitForm = () => {
    treeRef.value.validate((valid: boolean) => {
        if (valid) {
            buttonLoading.value = true;
            if (form.value.id != null) {
                updateTree(form.value).then(() => {
                    proxy?.$modal.msgSuccess("修改成功");
                    dialog.visible = false;
                    getList();
                }).finally(() => {
                    buttonLoading.value = false;
                });
            } else {
                addTree(form.value).then(() => {
                    proxy?.$modal.msgSuccess("新增成功");
                    dialog.visible = false;
                    getList();
                }).finally(() => {
                    buttonLoading.value = false;
                });
            }
        }
    });
}
/** 删除按钮操作 */
const handleDelete = (row: DemoTreeVO) => {
    proxy?.$modal.confirm('是否确认删除测试单表编号为"' + row.id + '"的数据项?').then(() => {
        loading.value = true;
        return delTree(row.id);
    }).then(() => {
        loading.value = false;
        getList();
        proxy?.$modal.msgSuccess("删除成功");
    }).finally(() => {
        loading.value = false;
    });
}
onMounted(() => {
    getList()
})
</script>
