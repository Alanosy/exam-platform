import copyText from './common/copyText';
import { hasPermi, hasRoles } from './permission';
import { App } from 'vue';

export default (app: App) => {
  app.directive('copyText', copyText);
  app.directive('hasPermi', hasPermi);
  app.directive('hasRoles', hasRoles);
};
