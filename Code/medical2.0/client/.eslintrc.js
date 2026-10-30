module.exports = {
  root: true,
  env: {
    node: true
  },
  globals: {
    // CDN externals (vue.config.js externals + public/lib) and the
    // window.currentUser global set by the home pages. Declaring known
    // globals only; no lint rule is disabled.
    Vue: 'readonly',
    Vuex: 'readonly',
    VueRouter: 'readonly',
    VueI18n: 'readonly',
    ELEMENT: 'readonly',
    echarts: 'readonly',
    G2Plot: 'readonly',
    UMYUI: 'readonly',
    jspdf: 'readonly',
    currentUser: 'readonly'
  },

  extends: [
    'plugin:vue/essential',
    'eslint:recommended'
  ],
  parserOptions: {
    parser: '@babel/eslint-parser'
  },
  rules: {
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off'
  }
}