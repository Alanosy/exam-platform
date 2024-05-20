import zh from '../../lang/zh';

const customTranslate = (template: any, replacements: any) => {
  replacements = replacements || {};
  template = zh[template] || template;
  return template.replace(/{([^}]+)}/g, function (_: any, key: any) {
    return replacements[key] || '{' + key + '}';
  });
};

export const translateModule = {
  translate: ['value', customTranslate]
};

export default translateModule;
