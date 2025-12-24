/// <reference types="vite/client" />
declare module "*.vue" {
    import type { DefineComponent } from "vue"
    const component: DefineComponent<{}, {}, any>
    export default component
}
interface ImportMetaEnv {
    VITE_APP_TITLE: string
    VITE_APP_PORT: string
    VITE_APP_BASE_URL: string
}
