import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/user',
      name: 'user',
      component: () => import('../views/user/UserLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'user-goods', component: () => import('../views/user/UserGoods.vue') },
        { path: 'cart', name: 'user-cart', component: () => import('../views/user/UserCart.vue') },
        { path: 'address', name: 'user-address', component: () => import('../views/user/UserAddress.vue') },
        { path: 'trade', name: 'user-trade', component: () => import('../views/user/UserTrade.vue') },
        { path: 'info', name: 'user-info', component: () => import('../views/user/UserInfo.vue') },
        { path: 'bg', name: 'user-bg', component: () => import('../views/user/UserBg.vue') },
        { path: 'vip', name: 'user-vip', component: () => import('../views/user/UserVip.vue') },
        { path: 'merchant/:userId', name: 'user-merchant', component: () => import('../views/user/MerchantProfile.vue') },
      ],
    },
    {
      path: '/merchant',
      name: 'merchant',
      component: () => import('../views/merchant/MerchantLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'merchant-goods', component: () => import('../views/merchant/MerchantGoods.vue') },
        { path: 'trade', name: 'merchant-trade', component: () => import('../views/merchant/MerchantTrade.vue') },
        { path: 'ai', name: 'merchant-ai', component: () => import('../views/merchant/MerchantAi.vue') },
      ],
    },
    {
      path: '/manager',
      name: 'manager',
      component: () => import('../views/manager/ManagerLayout.vue'),
      meta: { requiresAdmin: true },
      children: [
        { path: '', redirect: '/manager/user', name: 'manager-redirect' },
        { path: 'user', name: 'manager-user', component: () => import('../views/manager/ManagerUser.vue') },
        { path: 'goods', name: 'manager-goods', component: () => import('../views/manager/ManagerGoods.vue') },
        { path: 'trade', name: 'manager-trade', component: () => import('../views/manager/ManagerTrade.vue') },
        { path: 'cart', name: 'manager-cart', component: () => import('../views/manager/ManagerCart.vue') },
        { path: 'address', name: 'manager-address', component: () => import('../views/manager/ManagerAddress.vue') },
        { path: 'vip-config', name: 'manager-vip-config', component: () => import('../views/manager/ManagerVipConfig.vue') },
        { path: 'vip-trade', name: 'manager-vip-trade', component: () => import('../views/manager/ManagerVipTrade.vue') },
        { path: 'ai', name: 'manager-ai', component: () => import('../views/manager/ManagerAi.vue') },
        { path: 'history-goods', name: 'manager-history-goods', component: () => import('../views/manager/ManagerHistoryGoods.vue') },
      ],
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/ai-recommend',
      name: 'ai-recommend',
      component: () => import('../views/AIRecommend.vue'),
    },
    {
      path: '/background',
      name: 'background',
      component: () => import('../views/BackgroundView.vue'),
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  if (to.matched.some(r => r.meta.requiresAdmin)) {
    if (userStore.isLoggedIn && userStore.isAdmin) {
      next()
    } else {
      next('/login')
    }
    return
  }

  if (to.matched.some(r => r.meta.requiresAuth)) {
    if (userStore.isLoggedIn) {
      next()
    } else {
      next('/login')
    }
    return
  }

  next()
})

export default router