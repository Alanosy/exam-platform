import Cookies from 'js-cookie';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import en from 'element-plus/es/locale/lang/en';

export const useAppStore = defineStore('app', () => {
	const sidebarStatus = Cookies.get('sidebarStatus');
	const sidebar = reactive({
		opened: sidebarStatus ? !!+sidebarStatus : true,
		withoutAnimation: false,
		hide: false
	});
	const device = ref<string>('desktop');
	const size = ref(Cookies.get('size') || 'default');
	// 语言
	const language = ref(Cookies.get('language'));
	const locale = computed(() => {
		if (language?.value == 'en') {
			return en;
		} else {
			return zhCn;
		}
	});

	const toggleSideBar = (withoutAnimation?: boolean) => {
		if (sidebar.hide) {
			return false;
		}

		sidebar.opened = !sidebar.opened;
		sidebar.withoutAnimation = withoutAnimation as boolean;
		if (sidebar.opened) {
			Cookies.set('sidebarStatus', '1');
		} else {
			Cookies.set('sidebarStatus', '0');
		}
	};

	const closeSideBar = ({ withoutAnimation }: any): void => {
		Cookies.set('sidebarStatus', '0');
		sidebar.opened = false;
		sidebar.withoutAnimation = withoutAnimation;
	};
	const toggleDevice = (d: string): void => {
		device.value = d;
	};
	const setSize = (s: string): void => {
		size.value = s;
		Cookies.set('size', s);
	};
	const toggleSideBarHide = (status: boolean): void => {
		sidebar.hide = status;
	};

	const changeLanguage = (val: string): void => {
		language.value = val;
	};

	return {
		device,
		sidebar,
		language,
		locale,
		size,
		changeLanguage,
		toggleSideBar,
		closeSideBar,
		toggleDevice,
		setSize,
		toggleSideBarHide
	};
});

export default useAppStore;
