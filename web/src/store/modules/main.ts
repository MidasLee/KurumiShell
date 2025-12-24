import { defineStore } from 'pinia'
import { ref } from 'vue'

const useMainStore = defineStore('main', () => {
    const counter = ref(0)

    const increment = () => {
        counter.value++
    }

    return {
        counter,
        increment
    }
})

export default useMainStore