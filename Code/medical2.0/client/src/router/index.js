const Vue = window.Vue
import Router from 'vue-router'
import LoginView from '@/views/login/index'
import { getLocalToken, getLocalRouters } from '@/utils/auth'
import { routerTree, handleFamily } from '@/utils/routerTree'
import { isPlaceholderRoute, shouldHidePlaceholderRoutes } from '@/utils/placeholderRoutes'
// Element UI Đã qua CDN Đang tải
const ELEMENT = window.ELEMENT


const _import = require('./_import_' + process.env.NODE_ENV)
Vue.use(VueRouter)

//Tải theo nhu cầu
/**
 * Đôi khi ta muốn gói tất cả component trong cùng một route vào cùng một async chunk.
 * Chỉ cần đặt tên chunk, cung cấp tham số thứ ba của require.ensure làm tên chunk.
 * Webpack sẽ gói tất cả module async trong cùng chunk vào một khối async,
 * điều này cũng có nghĩa là không cần liệt kê rõ dependency của require.ensure (truyền mảng rỗng là được).
 */
//const Index = resolve => require(['@/views/index'], resolve)
//const foodList = r => require.ensure([], () => r(require('@/page/foodList')), 'foodList');

const router = new VueRouter({
  routes: [
    {
      path: '/login',
      name: "login",
      component: LoginView,
      meta: { requiresAuth: false }
    },{
      path: '/',
      component: resolve => require(['@/views/home/index'], resolve),
      meta: { requiresAuth: true }
    },
    {
      path: '/preview/:reportId',
      component: _import('report/components/PreviewEject'),
      hidden: true
    },
    {
      path:'/charge',
      name:'charge',
      component: _import('charge/index')
    },{
      path:'/chargeaccountreport',
      name:'charchargeaccountreportge',
      component: _import('chargeaccountreport/index')
    },{
      path:'/chargecombinereport',
      name:'chargecombinereport',
      component: _import('chargecombinereport/index')
    }
  ]
});

generateIndexRouter();

// Kiểm tra token, chỉ chuyển hướng khi token tồn tại
router.beforeEach((to, from, next) => {
  let token = getLocalToken()
  /*
	if(to.meta.requiresAuth) {    //Kiểm tra xem có cần quyền đăng nhập không
		next()
	}
  */

  if(to.path === '/login'){
    next()
  }else{
    if(token){
      next()
    }else{
      if(to.path === '/'){
        next('/login')
      }else{
        router.app.$store.dispatch('menus/SetToPath',to.path );
        ELEMENT.Message({message:"Bạn chưa đăng nhập, vui lòng đăng nhập trước",type: 'warning', duration: 2 * 1000});
        next('/login')
      }
    }
  }


});

router.afterEach((to, from) => {
  // Thiết lập breadcrumb
  let breadCrumbItems = []
  /*  let homePageCrumb = {
	  title: 'Trang chủ',
	  to: '/'
	}
	breadCrumbItems.push(homePageCrumb)*/
  if(to.meta.nameFullPath) {
    let fullPathSplit = to.meta.nameFullPath.split('/')
    fullPathSplit.forEach(( item, index ) => {
      let routerBreadCrumb = {
        title: item,
        to: (index === fullPathSplit.length - 1 ? to.path : '')
      }
      breadCrumbItems.push(routerBreadCrumb)
    });
  }
  // Cập nhật vào state

  if (router.app.$store) {
    router.app.$store.dispatch('setBreadcurmbItems', breadCrumbItems)
  }
})

// Sinh route trang chủ
function generateIndexRouter() {
  if (!sessionStorage.routers || sessionStorage.getItem('indexRouterGenerated') === 'true') {
    return
  }
  let indexRouter = {
    path: '/',
    name: "/",
    component: _import('home/index'),
    children: [
      ...generateChildRouters()
    ]
  }
  // router.addRoutes([indexRouter])
  router.addRoute('/', indexRouter)
  sessionStorage.setItem('indexRouterGenerated', 'true')
}

// Sinh route lồng nhau (route con)
function generateChildRouters() {
  let routers = getLocalRouters()
  let routerTreeArr = routerTree(routers)
  // Đụng độ path không phân biệt hoa/thường (vd 'myTask' vs 'mytask'):
  // vue-router mặc định match không phân biệt hoa/thường nên route đăng ký
  // trước thắng và trang thật không bao giờ render. Chỉ các route có path
  // đụng độ mới bật caseSensitive (cần cả 2 phía).
  const lowerPathCounts = {}
  routers.forEach((r) => {
    if (r.code && r.url) {
      const k = String(r.url).toLowerCase()
      lowerPathCounts[k] = (lowerPathCounts[k] || 0) + 1
    }
  })
  let childRouters = []
  for(let router of routers) {
    // Blocker 5a: bản production không đăng ký route placeholder; dev/test vẫn giữ để kiểm chứng.
    if(router.code && shouldHidePlaceholderRoutes() && isPlaceholderRoute(router.code)) {
      continue
    }
    if(router.code) {
      let routerProps = JSON.parse(router.properties)
      let childRouter = {
        path: router.url,
        name: router.code,
        component: _import(router.code + '/index'),
        caseSensitive: !!router.url && lowerPathCounts[String(router.url).toLowerCase()] > 1,
        meta: {
          name: router.name,
          cssClass: routerProps.cssClass,
          routerId: router.id,
          parentId: router.parent.id,
          family: handleFamily(routerTreeArr, item => item.id === router.id),
          requiresAuth: routerProps.meta.requiresAuth,
          nameFullPath: routerProps.nameFullPath,
          linkUrl: routerProps.linkUrl
        }
      };
      childRouters.push(childRouter)
    }
  }
  let redirectRouter = {
    path: 'redirect',
    component: _import('redirect/index')
  }
  childRouters.push(redirectRouter)


  return childRouters
}

// Tránh route trùng lặp
const originalPush = VueRouter.prototype.push

VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => {
    if (err.name !== 'NavigationDuplicated') throw err
  })
}

export default router;
