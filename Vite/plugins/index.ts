import vue from '@vitejs/plugin-vue';
import createUnoCss from './unocss';
import createAutoImport from './auto-import';
import createComponents from './components';
import createIcons from './icons';
import createSvgIconsPlugin from './svg-icon';
import path from 'path';

export default (viteEnv, isBuild = false): [] => {
	const vitePlusgins: any = [];
	vitePlusgins.push(vue());
	vitePlusgins.push(createUnoCss());
	vitePlusgins.push(createAutoImport(path));
	vitePlusgins.push(createComponents(path));
	vitePlusgins.push(createIcons());
	vitePlusgins.push(createSvgIconsPlugin(path));
	return vitePlusgins;
};
