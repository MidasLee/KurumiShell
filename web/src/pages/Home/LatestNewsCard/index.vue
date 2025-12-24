<script setup lang="ts">
import { NScrollbar, NTag } from 'naive-ui'

interface NewsItem {
  title: string
  date: string
  tag: "default" | "error" | "warning" | "success" | "primary" | "info"
  tagContent: string
}

const data: NewsItem[] = [
    {
        title: 'XVERSE-70B-V3 千亿级多模态大模型正式开源，支持视频理解与生成',
        date: '2025-06-18',
        tag: 'info',
        tagContent: '动态'
    },
    {
        title: '2025全球大模型创新挑战赛报名通道开启，总奖金池达500万元',
        date: '2025-05-25',
        tag: 'success',
        tagContent: '活动'
    },
    {
        title: '通义千问4.0正式发布，推理效率提升300%，支持千亿级上下文',
        date: '2025-04-10',
        tag: 'warning',
        tagContent: '新闻'
    },
    {
        title: '书生·浦语5.0开源，首次实现通用大模型端侧实时推理',
        date: '2025-03-22',
        tag: 'info',
        tagContent: '动态'
    },
    {
        title: '2025 AI大模型产业峰会将于7月在上海举办，聚焦落地应用',
        date: '2025-06-08',
        tag: 'success',
        tagContent: '活动'
    },
    {
        title: 'MathGLM 3.0发布，数学推理准确率突破98%，覆盖全学段数理问题',
        date: '2025-02-15',
        tag: 'warning',
        tagContent: '新闻'
    },
    {
        title: 'SWIFT 2.0框架升级，支持多模型协同部署，资源消耗降低40%',
        date: '2025-05-12',
        tag: 'info',
        tagContent: '动态'
    },
    {
        title: '2025中文多模态语音识别大赛结果公布，准确率创历史新高',
        date: '2025-04-28',
        tag: 'warning',
        tagContent: '新闻'
    }
]
</script>

<template>
    <div class="latest-news-card-div">
        <div class="head-div">
            <span>最新动态</span>
        </div>
        <div class="content-div">
            <n-scrollbar>
                <div v-for="(item, index) in data" class="content-item-div" :key="index">
                <div class="title-div">
                    <span class="title-span">{{ item.title }}</span>
                </div>
                <div class="date-div">
                    <span class="date-span">{{ item.date }}</span>
                </div>
                <div class="tag-div">
                    <n-tag :type="item.tag">
                        {{ item.tagContent }}
                    </n-tag>
                </div>
            </div>
            </n-scrollbar>
            
        </div>
    </div>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>