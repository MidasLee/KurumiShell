<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NButton, NIcon } from 'naive-ui'
import { CountUp } from 'countup.js'

const countUpDiv = ref<HTMLElement | null>(null)

interface Props {
    item: any
}

const props = withDefaults(defineProps<Props>(), {
    item: () => { }
})

function renderIcon() {
    return h(NIcon, { size: 40, color: "#0e7a0d" }, { default: () => h(props.item.icon) })
}

const initCountUp = () => {
    const countUp = new CountUp(countUpDiv.value as HTMLElement, props.item.text, {
        startVal: 0,
        duration: 5,
    })
    if (!countUp.error) {
        countUp.start()
    } else {
        console.error(countUp.error)
    }
}

onMounted(() => {
    initCountUp()
})
</script>

<template>
    <div class="statistic-card-div">
        <div class="head-div">
            <div class="icon-div">
                <component :is="renderIcon()" />
                <span>{{ item.name }}</span>
            </div>
            <div class="button-div">
                <n-button tertiary type="primary" round size="small">
                    详情
                </n-button>
            </div>
        </div>
        <div class="content-div">
            <div v-if="item.status === 'up'" class="up-icon-div">
                <svg class="icon" aria-hidden="true">
                    <use xlink:href="#icon-up"></use>
                </svg>
            </div>
            <div v-else class="down-icon-div">
                <svg class="icon" aria-hidden="true">
                    <use xlink:href="#icon-down"></use>
                </svg>
            </div>
            <div ref="countUpDiv" class="num-div"></div>
        </div>
        <div class="foot-div">
            <div v-if="item.status === 'up'" class="arrow-up-icon-div">
                <svg class="icon" aria-hidden="true">
                    <use xlink:href="#icon-arrow-up"></use>
                </svg>
            </div>
            <div v-else class="arrow-down-icon-div">
                <svg class="icon" aria-hidden="true">
                    <use xlink:href="#icon-arrow-down"></use>
                </svg>
            </div>
            <div class="description-div">
                {{ props.item.description }}
            </div>
        </div>
    </div>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>