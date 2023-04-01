import { createApp } from 'vue';
import Cookies from 'js-cookie';
// element-plus
import ElementPlus from 'element-plus';
import locale from 'element-plus/lib/locale/lang/zh-cn';

// global css
import 'uno.css';
import '@/assets/styles/index.scss';
import 'element-plus/theme-chalk/dark/css-vars.css';

// App、router、store
import App from './App.vue';
import store from './store';
import router from './router';

// 自定义指令
import directive from './directive';

// 注册插件
import plugins from './plugins/index'; // plugins
import { download } from '@/utils/request';

// 预设动画
import animate from './animate';

// svg图标
import 'virtual:svg-icons-register';
import ElementIcons from '@/plugins/svgicon';

// permission control
import './permission';

import { useDict } from '@/utils/dict';
import { getConfigKey, updateConfigByKey } from '@/api/system/config';
import { parseTime, addDateRange, handleTree, selectDictLabel, selectDictLabels } from '@/utils/ruoyi';

const app = createApp(App);
// 全局方法挂载
app.config.globalProperties.useDict = useDict;
app.config.globalProperties.getConfigKey = getConfigKey;
app.config.globalProperties.updateConfigByKey = updateConfigByKey;
app.config.globalProperties.download = download;
app.config.globalProperties.parseTime = parseTime;
app.config.globalProperties.handleTree = handleTree;
app.config.globalProperties.addDateRange = addDateRange;
app.config.globalProperties.selectDictLabel = selectDictLabel;
app.config.globalProperties.selectDictLabels = selectDictLabels;
app.config.globalProperties.animate = animate;

app.use(ElementIcons);
app.use(router);
app.use(store);
app.use(plugins);
// 自定义指令
directive(app);

// 使用element-plus 并且设置全局的大小
app.use(ElementPlus, {
	locale: locale,
	// 支持 large、default、small
	size: Cookies.get('size') || 'default'
});

// 修改 el-dialog 默认点击遮照为不关闭
(app._context.components.ElDialog as any).props.closeOnClickModal.default = false;

app.mount('#app');
