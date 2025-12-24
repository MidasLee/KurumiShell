// src/types/v-md-editor.d.ts
declare module '@kangc/v-md-editor' {
  import { App, Plugin } from 'vue'
  const VueMarkdownEditor: {
    install: (app: App) => void
    use: (theme: any, options?: any) => void
  }
  export default VueMarkdownEditor
}

declare module '@kangc/v-md-editor/lib/preview' {
  import { App, Plugin } from 'vue'
  const VMdPreview: {
    install: (app: App) => void
    use: (theme: any, options?: any) => void
  }
  export default VMdPreview
}

declare module '@kangc/v-md-editor/lib/theme/github.js' {
  const githubTheme: any
  export default githubTheme
}

declare module '@kangc/v-md-editor/lib/theme/vuepress.js' {
  const vuepressTheme: any
  export default vuepressTheme
}