<script setup lang="ts">
import { h, ref, watch, computed } from 'vue'
import type { Component } from 'vue'
import { NConfigProvider, NLayout, NLayoutSider, NLayoutHeader, NButton, NImage, NIcon, NMenu, NAvatar, NTabs, NTab, createDiscreteApi, NDropdown, NBreadcrumb, NBreadcrumbItem } from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import { RouterLink, useRouter, useRoute } from 'vue-router'
import type { TabItem } from '@/dto/TabItem.d.ts'
import useTabStore from '@/store/modules/useTabStore.ts'
import { storeToRefs } from 'pinia'
import useUserStore from '@/store/modules/useUserStore.ts'
import type { TabObject } from '@/dto/TabObject.d.ts'
import {
    UserMultiple,
    UserProfile,
    Logout,
    Home,
    Tools
} from '@vicons/carbon'
import {
    TerminalOutline,
    Refresh
} from '@vicons/ionicons5'
import {
    BrandDocker,
    Markdown,
    Maximize
} from '@vicons/tabler'
import {
    MenuFoldOutlined,
    MenuUnfoldOutlined
} from '@vicons/antd'
import kurumiShellImg from '@/assets/images/kurumi-shell.png'
import kurumiImg from '@/assets/images/kurumi.png'

const router = useRouter()
const route = useRoute()

const userStore = useUserStore()
const { userInfo } = storeToRefs(userStore)
const userId = computed(() => {
    return userInfo.value?.id
})

const tabStore = useTabStore()
const { tabObjects } = storeToRefs(tabStore)

const collapsed = ref<boolean>(false)

const menuRouteValue = ref<string>('/Home')
const tabRouteValue = ref<string>('/Home')

const { message } = createDiscreteApi(
    ['message']
)

const pushRouteToTabs = () => {
    const tabItem: TabItem = {
        title: route.meta.title as string,
        name: route.name as string,
        path: route.path,
        fullPath: route.fullPath
    }
    tabStore.addTab(userId.value, tabItem)
}

const currentTabs = computed(() => {
    const tabObject = tabObjects.value.find((item: TabObject) => item.userId === userId.value)
    if (tabObject === undefined) {
        return []
    }
    return tabObject.tabs
})

watch(() => route, () => {
    const tab = currentTabs.value.find((item: TabItem) => item.path === route.path)
    if (tab === undefined) {
        pushRouteToTabs()
    }
    menuRouteValue.value = route.path as string
    tabRouteValue.value = route.path as string
},
    {
        deep: true,
        immediate: true
    }
)

function renderIcon(icon: Component) {
    return () => h(NIcon, null, { default: () => h(icon) })
}

const avatarDropdownOptions = [
    {
        label: '用户管理',
        key: 'profile',
        icon: renderIcon(UserMultiple),
        props: {
            onClick: () => {
                router.push('/UserManagement')
            }
        }
    },
    {
        label: '用户资料',
        key: 'editProfile',
        icon: renderIcon(UserProfile),
        props: {
            onClick: () => {
                message.success('用户资料')
            }
        }
    },
    {
        label: '退出登录',
        key: 'logout',
        icon: renderIcon(Logout),
        props: {
            onClick: () => {
                const userStore = useUserStore()
                userStore.removeUserInfo()
                router.push('/Login')
                message.success('已退出登录')
            }
        }
    }
]

// 定义路由菜单项类型
interface RouteMenuItem {
    title: string
    icon: Component
}

// 路由对应的菜单配置映射
const routeMenuMap: Record<string, RouteMenuItem> = {
    '/Home': { title: '首页', icon: Home },
    '/UtilityTools': { title: '工具', icon: Tools },
    '/UtilityTools/SSH': { title: 'SSH', icon: TerminalOutline },
    '/UtilityTools/Docker': { title: 'Docker', icon: BrandDocker },
    '/UtilityTools/Markdown': { title: 'Markdown', icon: Markdown },
    '/UserManagement': { title: '用户管理', icon: UserMultiple }
}

const menuOptions: MenuOption[] = [
    {
        label: () =>
            h(
                RouterLink,
                {
                    to: {
                        path: '/Home'
                    }
                },
                { default: () => routeMenuMap['/Home'].title }
            ),
        key: '/Home',
        icon: renderIcon(routeMenuMap['/Home'].icon)
    },
    {
        label: routeMenuMap['/UtilityTools'].title,
        key: '/UtilityTools',
        icon: renderIcon(routeMenuMap['/UtilityTools'].icon),
        children: [
            {
                label: () =>
                    h(
                        RouterLink,
                        {
                            to: {
                                path: '/UtilityTools/SSH'
                            }
                        },
                        { default: () => routeMenuMap['/UtilityTools/SSH'].title }
                    ),
                key: '/UtilityTools/SSH',
                icon: renderIcon(routeMenuMap['/UtilityTools/SSH'].icon)
            },
            {
                label: () =>
                    h(
                        RouterLink,
                        {
                            to: {
                                path: '/UtilityTools/Docker'
                            }
                        },
                        { default: () => routeMenuMap['/UtilityTools/Docker'].title }
                    ),
                key: '/UtilityTools/Docker',
                icon: renderIcon(routeMenuMap['/UtilityTools/Docker'].icon)
            },
            {
                label: () =>
                    h(
                        RouterLink,
                        {
                            to: {
                                path: '/UtilityTools/Markdown'
                            }
                        },
                        { default: () => routeMenuMap['/UtilityTools/Markdown'].title }
                    ),
                key: '/UtilityTools/Markdown',
                icon: renderIcon(routeMenuMap['/UtilityTools/Markdown'].icon)
            }
        ]
    },
    {
        label: () =>
            h(
                RouterLink,
                {
                    to: {
                        path: '/UserManagement'
                    }
                },
                { default: () => routeMenuMap['/UserManagement'].title }
            ),
        key: '/UserManagement',
        icon: renderIcon(routeMenuMap['/UserManagement'].icon)
    }
]

// 生成面包屑数据的计算属性
const breadcrumbs = computed(() => {
    const path = route.path
    const segments = path.split('/').filter(segment => segment)
    const breadcrumbItems = []

    // 添加首页
    if (routeMenuMap['/Home']) {
        breadcrumbItems.push({
            title: 'KurumiShell',
            path: '/Home'
        })
    }

    if (segments.length > 0) {
        let currentPath = ''
        for (const segment of segments) {
            currentPath += `/${segment}`
            // 确保currentPath是routeMenuMap的有效键
            if (currentPath in routeMenuMap) {
                breadcrumbItems.push({
                    title: routeMenuMap[currentPath].title,
                    path: currentPath
                })
            }
        }
    }

    return breadcrumbItems
})

const collapsedButtonClickHandler = () => {
    collapsed.value = !collapsed.value
}

const refreshButtonClickHandler = () => {
    location.reload()
}

const maxWindowButtonClickHandler = () => {
    document.body.requestFullscreen()
}

const nTabsBeforeLeaveHandler = (value: string) => {
    router.push(value)
    return true
}

const nTabsCloseHandler = (value: string) => {
    if (value === '/Home') {
        message.warning('首页不可关闭')
    } else {
        tabStore.removeTab(
            userId.value,
            value
        )
        const path = currentTabs.value[currentTabs.value.length - 1].path
        if (value === route.path) {
            if (currentTabs.value.length === 1) {
                menuRouteValue.value = path
                tabRouteValue.value = path
                router.push(path)
            } else {
                router.go(-1)
            }
        }
    }
}

const breadcrumbItemClickHandler = (path: string) => {
    if (path === '/UtilityTools') {
        return
    } else {
        router.push(path)
    }
}

</script>

<template>
    <n-config-provider>
        <n-layout has-sider>
            <n-layout-sider :collapsed="collapsed" show-trigger collapse-mode="width" :collapsed-width="64" :width="240"
                :native-scrollbar="false" @collapse="collapsed = true" @expand="collapsed = false">
                <div class="title-div">
                    <n-image preview-disabled :src="kurumiShellImg" />
                    <div class="title-text-div" v-if="collapsed === false">
                        <h2>KurumiShell</h2>
                    </div>
                </div>
                <n-menu v-model:value="menuRouteValue" :default-value="'home'" :collapsed="collapsed"
                    :default-expand-all="true" :collapsed-width="64" :collapsed-icon-size="22" :options="menuOptions" />
            </n-layout-sider>
            <n-layout>
                <n-layout-header>
                    <div class="header-div">
                        <div class="left-div">
                            <n-button text v-if="collapsed" @click="collapsedButtonClickHandler">
                                <template #icon>
                                    <n-icon>
                                        <MenuUnfoldOutlined />
                                    </n-icon>
                                </template>
                            </n-button>
                            <n-button text v-else @click="collapsedButtonClickHandler">
                                <template #icon>
                                    <n-icon>
                                        <MenuFoldOutlined />
                                    </n-icon>
                                </template>
                            </n-button>
                            <n-button text @click="refreshButtonClickHandler">
                                <template #icon>
                                    <n-icon>
                                        <Refresh />
                                    </n-icon>
                                </template>
                            </n-button>
                            <n-breadcrumb>
                                <n-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index"
                                    @click="breadcrumbItemClickHandler(item.path)">
                                    {{ item.title }}
                                </n-breadcrumb-item>
                            </n-breadcrumb>
                        </div>
                        <div class="right-div">
                            <n-button text @click="maxWindowButtonClickHandler">
                                <template #icon>
                                    <n-icon>
                                        <Maximize />
                                    </n-icon>
                                </template>
                            </n-button>
                            <n-dropdown :options="avatarDropdownOptions">
                                <n-avatar round :size="34" :src="kurumiImg" />
                            </n-dropdown>
                        </div>
                    </div>
                </n-layout-header>
                <n-layout>
                    <n-tabs @before-leave="nTabsBeforeLeaveHandler" @close="nTabsCloseHandler"
                        v-model:value="tabRouteValue" type="card" :closable="true">
                        <n-tab v-for="(item, index) in currentTabs" :key="index" :name="item.path">
                            {{ item.title }}
                        </n-tab>
                    </n-tabs>
                    <div class="content-div">
                        <router-view v-slot="{ Component }">
                            <transition name="fade" mode="out-in">
                                <component :is="Component" />
                            </transition>
                        </router-view>
                    </div>
                </n-layout>
            </n-layout>
        </n-layout>
    </n-config-provider>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>
