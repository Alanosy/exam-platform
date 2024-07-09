import { Ref } from 'vue';

interface Options {
  title?: string;
}
interface Return {
  title: Ref<string>;
  visible: Ref<boolean>;
  openDialog: () => void;
  closeDialog: () => void;
}
export default (ops?: Options): Return => {
  const visible = ref(false);
  const title = ref(ops.title || '');

  const openDialog = () => {
    visible.value = true;
  };

  const closeDialog = () => {
    visible.value = false;
  };

  return {
    title,
    visible,

    openDialog,
    closeDialog
  };
};
