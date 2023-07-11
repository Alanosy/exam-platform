<template>
  <div class="user-info-head" @click="editCropper()">
    <img :src="options.img as string" title="点击上传头像" class="img-circle img-lg" />
    <el-dialog :title="title" v-model="open" width="800px" append-to-body @opened="modalOpened" @close="closeDialog">
      <el-row>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <vue-cropper
            ref="cropper"
            :img="options.img"
            :info="true"
            :autoCrop="options.autoCrop"
            :autoCropWidth="options.autoCropWidth"
            :autoCropHeight="options.autoCropHeight"
            :fixedBox="options.fixedBox"
            :outputType="options.outputType"
            @realTime="realTime"
            v-if="visible"
          />
        </el-col>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <div class="avatar-upload-preview">
            <img :src="options.previews.url" :style="options.previews.img" />
          </div>
        </el-col>
      </el-row>
      <br />
      <el-row>
        <el-col :lg="2" :md="2">
          <el-upload action="#" :http-request="requestUpload" :show-file-list="false" :before-upload="beforeUpload">
            <el-button>
              选择
              <el-icon class="el-icon--right">
                <Upload />
              </el-icon>
            </el-button>
          </el-upload>
        </el-col>
        <el-col :lg="{ span: 1, offset: 2 }" :md="2">
          <el-button icon="Plus" @click="changeScale(1)"></el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button icon="Minus" @click="changeScale(-1)"></el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button icon="RefreshLeft" @click="rotateLeft()"></el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :md="2">
          <el-button icon="RefreshRight" @click="rotateRight()"></el-button>
        </el-col>
        <el-col :lg="{ span: 2, offset: 6 }" :md="2">
          <el-button type="primary" @click="uploadImg()">提 交</el-button>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import "vue-cropper/dist/index.css";
import { VueCropper } from "vue-cropper";
import { uploadAvatar } from "@/api/system/user";
import useUserStore from "@/store/modules/user";

interface Options {
  img: string | ArrayBuffer | null; // 裁剪图片的地址
  autoCrop: boolean; // 是否默认生成截图框
  autoCropWidth: number; // 默认生成截图框宽度
  autoCropHeight: number; // 默认生成截图框高度
  fixedBox: boolean; // 固定截图框大小 不允许改变
  fileName: string;
  previews: any; // 预览数据
  outputType: string;
  visible: boolean;
}


const userStore = useUserStore();
const { proxy } = getCurrentInstance() as ComponentInternalInstance;

const open = ref(false);
const visible = ref(false);
const title = ref("修改头像");

const cropper = ref<any>({});
//图片裁剪数据
const options = reactive<Options>({
  img: userStore.avatar,
  autoCrop: true,
  autoCropWidth: 200,
  autoCropHeight: 200,
  fixedBox: true,
  outputType: "png",
  fileName: "",
  previews: {},
  visible: false
});

/** 编辑头像 */
const editCropper = () => {
  open.value = true;
};
/** 打开弹出层结束时的回调 */
const modalOpened = () => {
  visible.value = true;
};
/** 覆盖默认上传行为 */
const requestUpload = (): any => {
};
/** 向左旋转 */
const rotateLeft = () => {
  cropper.value.rotateLeft();
};
/** 向右旋转 */
const rotateRight = () => {
  cropper.value.rotateRight();
};
/** 图片缩放 */
const changeScale = (num: number) => {
  num = num || 1;
  cropper.value.changeScale(num);
};
/** 上传预处理 */
const beforeUpload = (file: any) => {
  if (file.type.indexOf("image/") == -1) {
    proxy?.$modal.msgError("文件格式错误，请上传图片类型,如：JPG，PNG后缀的文件。");
  } else {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => {
      options.img = reader.result;
      options.fileName = file.name;
    };
  }
};
/** 上传图片 */
const uploadImg = async () => {
  cropper.value.getCropBlob(async (data: any) => {
    let formData = new FormData();
    formData.append("avatarfile", data, options.fileName);
    const res = await uploadAvatar(formData);
    open.value = false;
    options.img = res.data.imgUrl;
    userStore.avatar = options.img as string
    proxy?.$modal.msgSuccess("修改成功");
    visible.value = false;
  });
};
/** 实时预览 */
const realTime = (data: any) => {
  options.previews = data;
};
/** 关闭窗口 */
const closeDialog = () => {
  options.img = userStore.avatar;
  options.visible = false;
};
</script>

<style lang="scss" scoped>
.user-info-head {
  position: relative;
  display: inline-block;
  height: 120px;
}

.user-info-head:hover:after {
  content: "+";
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  color: #eee;
  background: rgba(0, 0, 0, 0.5);
  font-size: 24px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  cursor: pointer;
  line-height: 110px;
  border-radius: 50%;
}
</style>
