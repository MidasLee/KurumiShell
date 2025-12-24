import { createApp } from 'vue'

import VMdPreview from '@kangc/v-md-editor/lib/preview'
import '@kangc/v-md-editor/lib/style/preview.css'
import githubTheme from '@kangc/v-md-editor/lib/theme/github.js'
import '@kangc/v-md-editor/lib/theme/style/github.css'
import hljs from 'highlight.js'

import VueMarkdownEditor from '@kangc/v-md-editor'
import '@kangc/v-md-editor/lib/style/base-editor.css'
import vuepressTheme from '@kangc/v-md-editor/lib/theme/vuepress.js'
import '@kangc/v-md-editor/lib/theme/style/vuepress.css'
import Prism from 'prismjs'

import './assets/style/main.scss'
import App from './App.vue'
import router from '@/router/index.js'
import pinia from '@/store/index.js'

import "@xterm/xterm/css/xterm.css"

VMdPreview.use(githubTheme, {
    Hljs: hljs,
})

VueMarkdownEditor.use(vuepressTheme, {
    Prism,
})

const app = createApp(App)
app.use(router)
app.use(pinia)
app.component('VMdPreview', VMdPreview)
app.component('VueMarkdownEditor', VueMarkdownEditor)
app.mount('#app')

document.title = import.meta.env.VITE_APP_TITLE

router.beforeEach((to: any, from: any, next: any) => {
    // 这里可写对路由相关操作
    next()
})