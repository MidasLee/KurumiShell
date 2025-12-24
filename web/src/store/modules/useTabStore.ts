import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { TabObject } from '@/dto/TabObject.d.ts'
import type { TabItem } from '@/dto/TabItem.d.ts'

const useTabStore = defineStore(
    'tab',
    () => {
        const tabObjects = ref<Array<TabObject>>([])
        const removeTab = (userId: string, name: string) => {
            const index: number = tabObjects.value.findIndex((tabObject: TabObject) => tabObject.userId === userId)
            if (index !== -1) {
                const tabIndex: number = tabObjects.value[index].tabs.findIndex((tabItem: TabItem) => tabItem.path === name)
                if (tabIndex !== -1) {
                    tabObjects.value[index].tabs.splice(tabIndex, 1)
                }
            }
        }
        const addTab = (userId: string, tabItem: TabItem) => {
            const index: number = tabObjects.value.findIndex((tabObject: TabObject) => tabObject.userId === userId)
            if (index === -1) {
                tabObjects.value.push({
                    userId: userId,
                    tabs: [tabItem]
                })
            } else {
                tabObjects.value[index].tabs.push(tabItem)
            }
        }
        return {
            tabObjects,
            removeTab,
            addTab
        }
    },
    {
        persist: true,
    }
)

export default useTabStore