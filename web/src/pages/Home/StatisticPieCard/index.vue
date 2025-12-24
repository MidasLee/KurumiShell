<script setup lang="ts">
import * as echarts from 'echarts'
import { ref, onMounted, onUnmounted, watch } from "vue"
import elementResizeDetectorMaker from 'element-resize-detector'

const erdInstance = elementResizeDetectorMaker()

const chartContainer = ref<HTMLElement | null>(null)

let chartInstance: echarts.ECharts | null = null

interface Props {
    data: Array<{ value: number, name: string }>
}

const props = withDefaults(defineProps<Props>(), {
    data: () => ([]) 
})

const getChart = () => {
    if (chartContainer.value) {
        type EChartsOption = echarts.EChartsOption
        if (!chartInstance) {
            chartInstance = echarts.init(chartContainer.value)
        }
        let option: EChartsOption

        option = {
            tooltip: {
                trigger: 'item'
            },
            legend: {
                orient: 'vertical',
                left: 'left'
            },
            series: [
                {
                    name: '资源数量',
                    type: 'pie',
                    radius: '80%',
                    data: props.data,
                    emphasis: {
                        itemStyle: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }
            ]
        }
        option && chartInstance.setOption(option)
        erdInstance.listenTo(chartContainer.value, () => {
            resizeChartInstance()
        })
    }
}

// 监听数据变化重新渲染图表
watch(() => props.data, () => {
    getChart()
}, { deep: true })

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
    getChart()
    window.addEventListener('resize', resizeChartInstance)
})

onUnmounted(() => {
    window.removeEventListener('resize', resizeChartInstance)
})
</script>

<template>
    <div class="statistic-pie-card-div">
        <div class="head-div">
            <span>接入统计</span>
        </div>
        <div class="content-div">
            <div ref="chartContainer" class="chart-div"></div>
        </div>
    </div>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>