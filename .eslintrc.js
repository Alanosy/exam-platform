module.exports = {
  env: {
    browser: true,
    node: true,
    es6: true
  },
  parser: 'vue-eslint-parser',
  extends: [
    'plugin:vue/vue3-recommended',
    './.eslintrc-auto-import.json',
    'plugin:@typescript-eslint/recommended',
    'prettier',
    'plugin:prettier/recommended'
  ],
  parserOptions: {
    ecmaVersion: '2020',
    sourceType: 'module',
    project: './tsconfig.*?.json',
    parser: '@typescript-eslint/parser'
  },
  plugins: ['vue', '@typescript-eslint', 'import', 'promise', 'node', 'prettier'],
  rules: {
    '@typescript-eslint/no-empty-function': 'off',
    '@typescript-eslint/no-explicit-any': 'off',
    '@typescript-eslint/no-unused-vars': 'off',
    '@typescript-eslint/no-this-alias': 'off',

    // vue
    'vue/multi-word-component-names': 'off',
    'vue/valid-define-props': 'off',
    'vue/no-v-model-argument': 'off',
    'prefer-rest-params': 'off',
    // prettier
    'prettier/prettier': 'error',
    '@typescript-eslint/ban-types': [
      'error',
      {
        // 关闭空类型检查 {}
        extendDefaults: true,
        types: {
          '{}': false,
          Function: false
        }
      }
    ]
  },
  globals: {
    DialogOption: 'readonly',
    OptionType: 'readonly'
  }
};
