import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/Login'
  },
  {
    path: '/Login',
    component: () => import("@/pages/Login/index.vue")
  },
  {
    path: '/layout',
    component: () => import("@/components/layout/index.vue"),
    children: [
      {
        path: "/UserManagement",
        name: "UserManagement",
        component: () => import("@/pages/UserManagement/index.vue"),
        meta: {
          index: '1',
          title: "用户管理",
          icon: ""
        }
      },
      {
        path: "/Home",
        name: "Home",
        component: () => import("@/pages/Home/index.vue"),
        meta: {
          index: '2',
          title: "首页",
          icon: ""
        }
      },
      {
        path: "/UtilityTools/SSH",
        name: "UtilityToolsSSH",
        component: () => import("@/pages/UtilityTools/SSH/index.vue"),
        meta: {
          index: '31',
          title: "SSH",
          icon: ""
        }
      },
      {
        path: "/UtilityTools/Docker",
        name: "UtilityToolsDocker",
        component: () => import("@/pages/UtilityTools/Docker/index.vue"),
        meta: {
          index: '32',
          title: "Docker",
          icon: ""
        }
      },
      {
        path: "/UtilityTools/Markdown",
        name: "UtilityToolsMarkdown",
        component: () => import("@/pages/UtilityTools/Markdown/index.vue"),
        meta: {
          index: '33',
          title: "Markdown",
          icon: ""
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

export default router