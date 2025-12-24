<script setup lang="ts">
import { NTabs, NTab } from 'naive-ui'
import { ref, onMounted, onUnmounted, watch } from "vue"
import * as echarts from 'echarts'
import elementResizeDetectorMaker from 'element-resize-detector'

const erdInstance = elementResizeDetectorMaker()

const tabName = ref<string>('markdownNotes')

const chartContainer = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

interface Props {
    data: {
        markdownNotesData: { label: string[], data: number[] },
        sshConnectionsData: { label: string[], data: number[] }
    }
}

const props = withDefaults(defineProps<Props>(), {
    data: () => ({
        markdownNotesData: { label: [], data: [] },
        sshConnectionsData: { label: [], data: [] }
    })
})

const getChart = (tab: string) => {
    if (chartContainer.value) {
        type EChartsOption = echarts.EChartsOption
        if (!chartInstance) {
            chartInstance = echarts.init(chartContainer.value)
        }
        let option: EChartsOption
        let label
        let data
        switch (tab) {
            case 'markdownNotes':
                label = props.data.markdownNotesData.label
                data = props.data.markdownNotesData.data
                break
            case 'sshConnections':
                label = props.data.sshConnectionsData.label
                data = props.data.sshConnectionsData.data
                break
            default:
                label = props.data.markdownNotesData.label
                data = props.data.markdownNotesData.data
                break
        }
        option = {
            grid: {
                left: '8%',
                right: '8%',
                top: '12%',
                bottom: '10%',
                containLabel: true
            },
            tooltip: {
                shadowBlur: 10,
                shadowColor: 'rgba(0, 0, 0, 0.5)',
                trigger: 'axis',
                axisPointer: {
                    type: 'shadow'
                },
                formatter: (params: any) => {
                    const { name, value } = params[0] || { name: '', value: 0 }
                    return `${name}: ${value}`
                }
            },
            xAxis: {
                type: 'category',
                data: label,
                axisLabel: {
                    formatter: (value: any, index: any) => {
                        if (value.length > 3) {
                            return value.substring(0, 3) + '...'
                        } else {
                            return value
                        }
                    }
                }
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    name: '数量',
                    data: data,
                    type: 'bar'
                }
            ],
            legend: {
                data: ['数量'],
            }
        }
        option && chartInstance.setOption(option, true)
        erdInstance.listenTo(chartContainer.value, () => {
            resizeChartInstance()
        })
    }
}

// 监听数据变化重新渲染图表
watch(() => props.data, () => {
    getChart(tabName.value)
}, { deep: true })

watch(() => tabName.value,
    () => {
        getChart(tabName.value)
    }
)

const resizeChartInstance = () => {
    if (chartInstance !== null) {
        chartInstance.resize({
            animation: {
                duration: 1000,
                easing: 'cubicInOut'
            },
        })
    }
}

onMounted(() => {
    getChart(tabName.value)
    window.addEventListener('resize', resizeChartInstance)
})

onUnmounted(() => {
    window.removeEventListener('resize', resizeChartInstance)
})

</script>

<template>
    <div class="classify-pie-card-div">
        <div class="head-div">
            <span>资源统计</span>
        </div>
        <div class="content-div">
            <div class="tab-div">
                <n-tabs v-model:value="tabName" type="line">
                    <n-tab name="markdownNotes">
                        笔记文件夹
                    </n-tab>
                    <n-tab name="sshConnections">
                        SSH连接分组
                    </n-tab>
                </n-tabs>
            </div>
            <div ref="chartContainer" class="chart-div">

            </div>
        </div>
    </div>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>