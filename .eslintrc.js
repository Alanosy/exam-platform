module.exports = {
  env: {
    browser: true,
    es2021: true,
    node: true
  },
  parser: 'vue-eslint-parser',
  extends: [
    'eslint:recommended',
    'plugin:vue/vue3-essential',
    'plugin:@typescript-eslint/recommended',
    './.eslintrc-auto-import.json',
    'plugin:prettier/recommended'
  ],
  parserOptions: {
    ecmaVersion: '2020',
    sourceType: 'module',
    parser: '@typescript-eslint/parser'
  },
  plugins: ['vue', '@typescript-eslint'],
  rules: {
    'vue/multi-word-component-names': 'off',
    '@typescript-eslint/no-empty-function': 'off',
    '@typescript-eslint/no-explicit-any': 'off',
    'vue/no-v-model-argument': 'off',
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
