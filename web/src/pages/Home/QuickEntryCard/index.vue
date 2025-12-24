<script setup lang="ts">
import { NScrollbar, NTooltip, NIcon } from 'naive-ui'
import {
    Document
} from '@vicons/carbon'

const data = [
    {
        title: '工作笔记',
        tip: '日常工作任务记录、会议纪要、项目进度跟踪、工作复盘、待办事项等'
    },
    {
        title: '学习笔记',
        tip: '专业知识整理、课程笔记、学习心得、知识点总结、错题记录、学习计划等'
    },
    {
        title: '生活随笔',
        tip: '日常感悟、生活琐事记录、旅行见闻、美食推荐、心情日记、待办清单等'
    },
    {
        title: '创意灵感',
        tip: '创意构思、灵感碎片、写作素材、设计想法、创意方案草稿、头脑风暴记录等'
    },
    {
        title: '技术文档',
        tip: '代码注释、技术方案、接口文档、故障排查记录、技术调研总结、开发规范等'
    },
    {
        title: '读书笔记',
        tip: '书籍摘要、读后感、金句摘抄、阅读计划、书籍推荐、章节重点梳理等'
    },
    {
        title: '财务记录',
        tip: '收支明细、预算规划、账单记录、理财笔记、消费分析、报销凭证整理等'
    },
    {
        title: '健康管理',
        tip: '运动记录、饮食打卡、体检报告、作息规划、健康知识整理、用药提醒等'
    }
]
</script>

<template>
    <div class="quick-entry-card-div">
        <div class="head-div">
            <span>快速入口</span>
        </div>
        <div class="content-div">
            <n-scrollbar>
                <n-tooltip v-for="(item, index) in data" :show-arrow="false" trigger="hover" :key="index">
                    <template #trigger>
                        <div class="content-item-div">
                            <div class="title-div">
                                <n-icon size="40" color="#0e7a0d">
                                    <Document />
                                </n-icon>
                                <span class="title-span">{{ item.title }}</span>
                            </div>
                            <div class="tip-div">
                                <span class="tip-span">{{ item.tip }}</span>
                            </div>
                        </div>
                    </template>
                    {{ item.tip }}
                </n-tooltip>
            </n-scrollbar>
        </div>
    </div>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>