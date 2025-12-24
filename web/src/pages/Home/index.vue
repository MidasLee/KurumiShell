<script setup lang="ts">
import { ref, onMounted, markRaw } from 'vue'
import { NScrollbar } from 'naive-ui'
import StatisticCard from '@/pages/Home/StatisticCard/index.vue'
import ClassifyBarCard from '@/pages/Home/ClassifyBarCard/index.vue'
import StatisticPieCard from '@/pages/Home/StatisticPieCard/index.vue'
import QuickEntryCard from '@/pages/Home/QuickEntryCard/index.vue'
import LatestNewsCard from '@/pages/Home/LatestNewsCard/index.vue'
import { getNoteList } from '@/pages/UtilityTools/Markdown/service/noteService.ts'
import { getFolderList } from '@/pages/UtilityTools/Markdown/service/folderService.ts'
import { getGroup } from '@/pages/UtilityTools/SSH/service/getGroup.ts'
import { getConnections } from '@/pages/UtilityTools/SSH/service/getConnections.ts'
import useUserStore from '@/store/modules/useUserStore.ts'
import {
    Document,
    Folder,
    Terminal,
    BareMetalServer
} from '@vicons/carbon'

// 统计卡片数据
let statisticData = ref<Array<Object>>([])
// 分类柱状图数据
let classifyBarData = ref<{
  markdownNotesData: { label: string[], data: number[] },
  sshConnectionsData: { label: string[], data: number[] }
}>({
  markdownNotesData: { label: [], data: [] },
  sshConnectionsData: { label: [], data: [] }
})
// 饼图数据
let pieData = ref<Array<{ value: number, name: string }>>([])

// 获取统计数据
const fetchStatisticData = async () => {
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo()
  
  try {
    // 获取笔记总数
    const notesResponse = await getNoteList({
      page: 1,
      pageSize: 1
    })
    const notesCount = notesResponse.data.total
    
    // 获取文件夹总数
    const foldersResponse = await getFolderList()
    const foldersCount = foldersResponse.data.length
    
    // 获取SSH分组总数
    const sshGroupsResponse = await getGroup()
    const sshGroupsCount = sshGroupsResponse.data.length
    
    // 获取SSH连接总数（云服务总数）
    const sshConnectionsResponse = await getConnections()
    const sshConnectionsCount = sshConnectionsResponse.data.length
    
    // 构造统计卡片数据
    statisticData.value = [
      {
        'icon': markRaw(Document),
        'name': '笔记总数',
        'status': 'up',
        'text': notesCount.toString(),
        'description': '笔记数量统计'
      },
      {
        'icon': markRaw(Folder),
        'name': '文件夹总数',
        'status': 'up',
        'text': foldersCount.toString(),
        'description': '文件夹数量统计'
      },
      {
        'icon': markRaw(Terminal),
        'name': 'SSH分组总数',
        'status': 'down',
        'text': sshGroupsCount.toString(),
        'description': 'SSH分组数量统计'
      },
      {
        'icon': markRaw(BareMetalServer),
        'name': 'SSH链接总数',
        'status': 'down',
        'text': sshConnectionsCount.toString(),
        'description': 'SSH链接数量统计'
      }
    ]
    
    // 构造饼图数据
    pieData.value = [
      { value: notesCount, name: '笔记' },
      { value: foldersCount, name: '文件夹' },
      { value: sshGroupsCount, name: 'SSH分组' },
      { value: sshConnectionsCount, name: 'SSH链接' }
    ]

    // 处理笔记文件夹数据
    const markdownFoldersLabel = foldersResponse.data.map((folder: any) => folder.name)
    const markdownFoldersData = foldersResponse.data.map((folder: any) => {
      // 统计每个文件夹中的笔记数量
      return notesResponse.data.data.filter((note: any) => note.folderId === folder.id).length
    })
    
    // 处理SSH连接分组数据
    const sshGroupsLabel = sshGroupsResponse.data.map((group: any) => group.name)
    const sshGroupsData = sshGroupsResponse.data.map((group: any) => {
      // 统计每个分组中的连接数量
      return group.connections ? group.connections.length : 0
    })
    
    classifyBarData.value = {
      markdownNotesData: { label: markdownFoldersLabel, data: markdownFoldersData },
      sshConnectionsData: { label: sshGroupsLabel, data: sshGroupsData }
    }
    
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

onMounted(() => {
  fetchStatisticData()
})

</script>

<template>
    <div class="home-div">
        <n-scrollbar>
            <div class="statistic-div">
                <div class="item-div" v-for="(item, index) in statisticData" :key="index">
                    <StatisticCard :item="item"></StatisticCard>
                </div>
            </div>
            <div class="echarts-div">
                <div class="left-div">
                    <ClassifyBarCard :data="classifyBarData"></ClassifyBarCard>
                </div>
                <div class="right-div">
                    <StatisticPieCard :data="pieData"></StatisticPieCard>
                </div>
            </div>
            <div class="entry-div">
                <div class="left-div">
                    <QuickEntryCard></QuickEntryCard>
                </div>
                <div class="right-div">
                    <LatestNewsCard></LatestNewsCard>
                </div>
            </div>
        </n-scrollbar>

    </div>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>