import mutations from './mutations'
import actions from './actions'
import getters from './getters'
const Vue = window.Vue
Vue.use(Vuex);

/**
 * Xử lý import module dưới thư mục modules
 */
let modules = {};
const modulesFiles = require.context('./modules', true, /\.js$/);
modulesFiles.keys().forEach(module => {
  const com = modulesFiles(module);
  
  // Lấy tên module
  const comName = module.replace(/^\.\/(.*)\.\w+$/, '$1');
  // Thêm đối tượng module
  modules[comName] = com.default;
})

const state = {
    showLoading: false,
    breadcrumbItems: []
};

export default new Vuex.Store({
    modules,
    state,
    mutations,
    actions,
    getters
});
