declare module "*.vue" {
    import { ComponentOptions } from "vue"
    const component: ComponentOptions
    export default component
}

declare module "*.scss" {
    const content: { [className: string]: string }
    export default content
}
