<script setup lang="ts">
import { ref, reactive, h } from 'vue'
import {
    NTabs,
    NTabPane,
    NInput,
    NButton,
    NSelect,
    NDataTable,
    NSpace,
    NIcon,
    NModal,
    NDescriptions,
    NDescriptionsItem,
    NSpin
} from 'naive-ui'
import { Search as SearchIcon, InformationCircleOutline as InfoCircleIcon, PricetagOutline as TagIcon, DownloadOutline as DownloadIcon } from '@vicons/ionicons5'
import { searchImages as searchDudubirdImages, getImageInfo } from '@/pages/UtilityTools/Docker/service/dudubirdService.ts'
import { searchImages as searchXuanyuanImages, getImageTags } from '@/pages/UtilityTools/Docker/service/xuanyuanService.ts'
import type { ImageResultDTO } from '@/pages/UtilityTools/Docker/dto/SearchImagesDTO.ts'
import type { ImageInfoDTO } from '@/pages/UtilityTools/Docker/dto/ImageInfoDTO.ts'
import type { XuanyuanImageResultDTO } from '@/pages/UtilityTools/Docker/dto/XuanyuanSearchDTO.ts'
import type { XuanyuanImageTagsDTO } from '@/pages/UtilityTools/Docker/dto/XuanyuanImageTagsDTO.ts'
import { createDiscreteApi } from 'naive-ui'

const { message } = createDiscreteApi(['message'])

// Tab页管理
const activeTab = ref('dudubird')

// 渡渡鸟页面相关状态
const searchParams = reactive({
    search: '',
    site: 'All',
    platform: 'All',
    sort: '名称排序'
})

// 搜索表单选项
const siteOptions = [
    { label: '全部站点', value: 'All' },
    { label: 'gcr.io', value: 'gcr.io' },
    { label: 'ghcr.io', value: 'ghcr.io' },
    { label: 'quay.io', value: 'quay.io' },
    { label: 'k8s.gcr.io', value: 'k8s.gcr.io' },
    { label: 'docker.io', value: 'docker.io' },
    { label: 'registry.k8s.io', value: 'registry.k8s.io' },
    { label: 'docker.elastic.co', value: 'docker.elastic.co' },
    { label: 'skywalking.docker.scarf.sh', value: 'skywalking.docker.scarf.sh' },
    { label: 'mcr.microsoft.com', value: 'mcr.microsoft.com' }
]

const platformOptions = [
    { label: '全部平台', value: 'All' },
    { label: 'linux/386', value: 'linux/386' },
    { label: 'linux/amd64', value: 'linux/amd64' },
    { label: 'linux/arm64', value: 'linux/arm64' },
    { label: 'linux/arm', value: 'linux/arm' },
    { label: 'linux/ppc64le', value: 'linux/ppc64le' },
    { label: 'linux/s390x', value: 'linux/s390x' },
    { label: 'linux/mips64le', value: 'linux/mips64le' },
    { label: 'linux/riscv64', value: 'linux/riscv64' },
    { label: 'linux/loong64', value: 'linux/loong64' }
]

const sortOptions = [
    { label: '名称排序', value: '名称排序' },
    { label: '镜像大小', value: '镜像大小' },
    { label: '浏览量', value: '浏览量' },
    { label: '最近同步', value: '最近同步' },
    { label: '最早同步', value: '最早同步' }
]

// 渡渡鸟页面相关状态
const searchResults = ref<ImageResultDTO[]>([])
let totalResults = ref<number>(0)
const loading = ref<boolean>(false)
let currentPage = ref<number>(1)
let pageSize = ref<number>(10)

// 镜像详情相关状态
const showImageDetail = ref<boolean>(false)
const currentImage = ref<ImageResultDTO | null>(null)
const imageInfo = ref<ImageInfoDTO | null>(null)
const loadingImageInfo = ref<boolean>(false)
const isPullCommandMode = ref<boolean>(false)

// 轩辕页面相关状态
const xuanyuanSearchParams = reactive({
    search: ''
})

const xuanyuanSearchResults = ref<XuanyuanImageResultDTO[]>([])
let xuanyuanTotalResults = ref<number>(0)
const xuanyuanLoading = ref<boolean>(false)
let xuanyuanCurrentPage = ref<number>(1)
let xuanyuanPageSize = ref<number>(10)

// 镜像标签相关状态
const showImageTagsModal = ref<boolean>(false)
const currentXuanyuanImage = ref<XuanyuanImageResultDTO | null>(null)
const imageTags = ref<XuanyuanImageTagsDTO | null>(null)
const loadingImageTags = ref<boolean>(false)

// 轩辕镜像详情相关状态
const showXuanyuanImageDetail = ref<boolean>(false)
const loadingXuanyuanImageDetail = ref<boolean>(false)
const isXuanyuanPullCommandMode = ref<boolean>(false)

// 搜索渡渡鸟镜像
const handleSearch = async () => {
    if (!searchParams.search.trim()) {
        message.warning('请输入搜索内容')
        return
    }

    try {
        loading.value = true
        const response = await searchDudubirdImages({
            search: searchParams.search,
            site: searchParams.site,
            platform: searchParams.platform,
            sort: searchParams.sort,
            page: 1,
            page_size: pageSize.value
        })
        searchResults.value = response.results
        totalResults.value = response.count
        currentPage.value = 1
    } catch (error) {
        message.error('搜索镜像失败，请稍后重试')
        console.error('搜索镜像失败:', error)
    } finally {
        loading.value = false
    }
}

// 搜索轩辕镜像
const handleXuanyuanSearch = async () => {
    if (!xuanyuanSearchParams.search.trim()) {
        message.warning('请输入搜索内容')
        return
    }

    try {
        xuanyuanLoading.value = true
        const response = await searchXuanyuanImages({
            imageName: xuanyuanSearchParams.search,
            page: 1,
            pageSize: xuanyuanPageSize.value
        })
        xuanyuanSearchResults.value = response.results
        xuanyuanTotalResults.value = response.total
        xuanyuanCurrentPage.value = 1
        message.success(`找到 ${response.total} 个镜像`)
    } catch (error) {
        message.error('搜索镜像失败，请稍后重试')
        console.error('搜索镜像失败:', error)
    } finally {
        xuanyuanLoading.value = false
    }
}

// 处理轩辕分页变化
const handleXuanyuanPageChange = async () => {
    try {
        xuanyuanLoading.value = true
        const response = await searchXuanyuanImages({
            imageName: xuanyuanSearchParams.search,
            page: xuanyuanCurrentPage.value,
            pageSize: xuanyuanPageSize.value
        })
        xuanyuanSearchResults.value = response.results
    } catch (error) {
        message.error('获取镜像列表失败，请稍后重试')
        console.error('获取镜像列表失败:', error)
    } finally {
        xuanyuanLoading.value = false
    }
}

// 处理渡渡鸟分页变化
const handlePageChange = async () => {
    try {
        loading.value = true
        const response = await searchDudubirdImages({
            ...searchParams,
            page: currentPage.value,
            page_size: pageSize.value
        })
        searchResults.value = response.results
    } catch (error) {
        message.error('搜索失败，请稍后重试')
        console.error('搜索镜像失败:', error)
    } finally {
        loading.value = false
    }
}

// 查看镜像详情
const viewImageDetail = async (image: ImageResultDTO) => {
    currentImage.value = image
    showImageDetail.value = true

    try {
        loadingImageInfo.value = true

        const response = await getImageInfo({ image_name: image.image_name })
        imageInfo.value = response
    } catch (error) {
        message.error('获取镜像详情失败，请稍后重试')
        console.error('获取镜像详情失败:', error)
    } finally {
        loadingImageInfo.value = false
    }
}

// 定义渡渡鸟表格列配置
const columns = [
    {
        title: '镜像名称',
        key: 'image_name',
        width: 400,
        render(row: ImageResultDTO) {
            return h('div', { style: 'display: flex; align-items: center;' }, [
                h('img', {
                    src: row.icon_path,
                    alt: row.image_name,
                    style: 'width: 24px; height: 24px; border-radius: 4px; margin-right: 8px;'
                }),
                h('span', { style: 'word-break: break-all;' }, row.image_name)
            ])
        }
    },
    {
        title: '来源',
        key: 'source',
        width: 150
    },
    {
        title: '平台',
        key: 'platform',
        width: 150
    },
    {
        title: '大小',
        key: 'size',
        width: 150
    },
    {
        title: '更新时间',
        key: 'collection_date',
        width: 180
    },
    {
        title: '操作',
        key: 'action',
        width: 200,
        render(row: ImageResultDTO) {
            return h('div', { style: 'display: flex; gap: 8px;' }, [
                h(NButton, {
                    type: 'primary',
                    size: 'small',
                    text: true,
                    onClick: async () => {
                        currentImage.value = row
                        isPullCommandMode.value = true
                        showImageDetail.value = true

                        try {
                            loadingImageInfo.value = true
                            const response = await getImageInfo({ image_name: row.image_name })
                            imageInfo.value = response
                        } catch (error) {
                            message.error('获取镜像详情失败，请稍后重试')
                            console.error('获取镜像详情失败:', error)
                        } finally {
                            loadingImageInfo.value = false
                        }
                    }
                }, {
                    icon: () => h(NIcon, null, { default: () => h(DownloadIcon) }),
                    default: () => '拉取命令'
                }),
                h(NButton, {
                    type: 'info',
                    size: 'small',
                    text: true,
                    onClick: () => {
                        isPullCommandMode.value = false
                        viewImageDetail(row)
                    }
                }, {
                    icon: () => h(NIcon, null, { default: () => h(InfoCircleIcon) }),
                    default: () => '详情'
                })
            ])
        }
    }
]

// 定义轩辕表格列配置
const xuanyuanColumns = [
    {
        title: '镜像名称',
        key: 'name',
        width: 400,
        render(row: XuanyuanImageResultDTO) {
            return h('div', { style: 'display: flex; align-items: flex-start; flex-direction: column;' }, [
                h('div', { style: 'display: flex; align-items: center;' }, [
                    h('img', { src: row.logo_url?.small || '', alt: row.name, style: 'width: 24px; height: 24px; border-radius: 4px; margin-right: 8px;' }),
                    h('span', { style: 'word-break: break-all;' }, row.name)
                ]),
                h('span', { style: 'font-size: 12px; color: #999; margin-left: 32px; word-break: break-all;' }, `ID: ${row.id}`)
            ])
        }
    },
    {
        title: '描述',
        key: 'short_description',
        width: 400
    },
    {
        title: '徽章',
        key: 'badge',
        width: 100,
        render(row: XuanyuanImageResultDTO) {
            return h('div', {
                class: 'badge',
                style: {
                    padding: '2px 8px',
                    borderRadius: '4px',
                    backgroundColor: row.badge === 'official' ? '#1890ff' : '#52c41a',
                    color: 'white',
                    fontSize: '12px'
                }
            }, row.badge === 'official' ? '官方' : '认证')
        }
    },
    {
        title: '拉取数',
        key: 'pull_count',
        width: 120
    },
    {
        title: '操作',
        key: 'action',
        width: 200,
        render(row: XuanyuanImageResultDTO) {
            return h('div', { style: 'display: flex; gap: 8px;' }, [
                h(NButton, {
                    type: 'primary',
                    size: 'small',
                    text: true,
                    onClick: () => {
                        currentXuanyuanImage.value = row
                        isXuanyuanPullCommandMode.value = true
                        showXuanyuanImageDetail.value = true
                    }
                }, {
                    icon: () => h(NIcon, null, { default: () => h(DownloadIcon) }),
                    default: () => '拉取命令'
                }),
                h(NButton, {
                    type: 'info',
                    size: 'small',
                    text: true,
                    onClick: () => {
                        isXuanyuanPullCommandMode.value = false
                        viewXuanyuanImageDetail(row)
                    }
                }, {
                    icon: () => h(NIcon, null, { default: () => h(InfoCircleIcon) }),
                    default: () => '详情'
                }),
                h(NButton, {
                    type: 'success',
                    size: 'small',
                    text: true,
                    onClick: () => viewImageTags(row)
                }, {
                    icon: () => h(NIcon, null, { default: () => h(TagIcon) }),
                    default: () => '标签'
                })
            ])
        }
    }
]

// 关闭镜像详情
const closeImageDetail = () => {
    showImageDetail.value = false
    isPullCommandMode.value = false
    currentImage.value = null
    imageInfo.value = null
}

// 查看镜像详情
const viewXuanyuanImageDetail = (row: XuanyuanImageResultDTO) => {
    currentXuanyuanImage.value = row
    showXuanyuanImageDetail.value = true
}

// 查看镜像标签
const viewImageTags = async (row: ImageResultDTO | XuanyuanImageResultDTO) => {
    let imageName: string
    if ('image_name' in row) {
        // DuduBird镜像类型
        currentImage.value = row as ImageResultDTO
        imageName = row.image_name
    } else {
        // 轩辕镜像类型
        currentXuanyuanImage.value = row as XuanyuanImageResultDTO
        imageName = row.name
    }
    showImageTagsModal.value = true
    loadingImageTags.value = true

    try {
        // 提取namespace和name
        let namespace = 'library'
        let name = imageName

        // 如果镜像名包含斜杠，则按斜杠分割得到namespace和name
        if (imageName.includes('/')) {
            const parts = imageName.split('/')
            if (parts.length === 2) {
                [namespace, name] = parts
            } else if (parts.length === 3) {
                namespace = `${parts[0]}/${parts[1]}`
                name = parts[2]
            }
        }

        const response = await getImageTags({ namespace, name })
        imageTags.value = response
    } catch (error) {
        message.error('获取镜像标签失败，请稍后重试')
        console.error('获取镜像标签失败:', error)
    } finally {
        loadingImageTags.value = false
    }
}

// 关闭标签弹窗
const closeImageTags = () => {
    showImageTagsModal.value = false
    currentXuanyuanImage.value = null
    imageTags.value = null
}

// 关闭轩辕镜像详情弹窗
const closeXuanyuanImageDetail = () => {
    showXuanyuanImageDetail.value = false
    currentXuanyuanImage.value = null
    isXuanyuanPullCommandMode.value = false
}
</script>

<template>
    <div class="docker-container">
        <n-tabs v-model:value="activeTab" type="line">
            <!-- 渡渡鸟页面 -->
            <n-tab-pane name="dudubird" tab="渡渡鸟">
                <div class="dudubird-content">
                    <!-- 搜索表单 -->
                    <div class="search-form">
                        <n-space>
                            <n-input v-model:value="searchParams.search" placeholder="搜索镜像名" clearable
                                @keyup.enter="handleSearch">
                                <template #prefix>
                                    <n-icon>
                                        <SearchIcon />
                                    </n-icon>
                                </template>
                            </n-input>
                            <n-select v-model:value="searchParams.site" :options="siteOptions" placeholder="选择站点" />
                            <n-select v-model:value="searchParams.platform" :options="platformOptions"
                                placeholder="选择平台" />
                            <n-select v-model:value="searchParams.sort" :options="sortOptions" placeholder="排序方式" />
                            <n-button type="primary" @click="handleSearch">
                                搜索
                            </n-button>
                        </n-space>
                    </div>

                    <!-- 搜索结果 -->
                    <div class="search-results">
                        <n-data-table :columns="columns" :data="searchResults" :loading="loading" :pagination="{
                            page: currentPage,
                            pageSize: pageSize,
                            itemCount: totalResults,
                            showSizePicker: true,
                            pageSizes: [10, 20, 50, 100],
                            'onUpdate:page': (page: number) => {
                                currentPage = page
                                handlePageChange()
                            },
                            'onUpdate:pageSize': (size: number) => {
                                pageSize = size
                                currentPage = 1
                                handlePageChange()
                            }
                        }" :flex-height="true" style="height: 100%" bordered />
                    </div>
                </div>
            </n-tab-pane>

            <!-- 轩辕页面 -->
            <n-tab-pane name="xuanyuan" tab="轩辕">
                <div class="xuanyuan-content">
                    <!-- 搜索表单 -->
                    <div class="search-form">
                        <n-space>
                            <n-input v-model:value="xuanyuanSearchParams.search" placeholder="搜索镜像名" clearable
                                @keyup.enter="handleXuanyuanSearch">
                                <template #prefix>
                                    <n-icon>
                                        <SearchIcon />
                                    </n-icon>
                                </template>
                            </n-input>
                            <n-button type="primary" @click="handleXuanyuanSearch">
                                搜索
                            </n-button>
                        </n-space>
                    </div>

                    <!-- 搜索结果 -->
                    <div class="search-results">
                        <n-data-table :columns="xuanyuanColumns" :data="xuanyuanSearchResults"
                            :loading="xuanyuanLoading" :pagination="{
                                page: xuanyuanCurrentPage,
                                pageSize: xuanyuanPageSize,
                                itemCount: xuanyuanTotalResults,
                                showSizePicker: true,
                                pageSizes: [10, 20, 50, 100],
                                'onUpdate:page': (page: number) => {
                                    xuanyuanCurrentPage = page
                                    handleXuanyuanPageChange()
                                },
                                'onUpdate:pageSize': (size: number) => {
                                    xuanyuanPageSize = size
                                    xuanyuanCurrentPage = 1
                                    handleXuanyuanPageChange()
                                }
                            }" :flex-height="true" style="height: 100%" bordered />
                    </div>
                </div>
            </n-tab-pane>
        </n-tabs>

        <!-- 镜像详情弹窗 -->
        <n-modal class="image-detail-modal" v-model:show="showImageDetail" preset="dialog" :close-on-esc="true"
            :close-on-clickoutside="true" :mask-closable="true" :title="isPullCommandMode ? '拉取命令' : '镜像详情'"
            @close="closeImageDetail" style="width: calc(100vw - 400px);">
            <n-spin :show="loadingImageInfo">
                <template #description>
                    加载中，请稍后……
                </template>
                <div class="image-detail-content">
                    <template v-if="!isPullCommandMode">
                        <n-descriptions bordered size="small" :column="1">
                            <n-descriptions-item label="原始镜像">{{ imageInfo?.source_image || '' }}</n-descriptions-item>
                            <n-descriptions-item label="国内镜像">{{ imageInfo?.domestic_image || ''
                                }}</n-descriptions-item>
                            <n-descriptions-item label="镜像ID">{{ imageInfo?.image_id || '' }}</n-descriptions-item>
                            <n-descriptions-item label="镜像标签">{{ imageInfo?.image_tag || '' }}</n-descriptions-item>
                            <n-descriptions-item label="大小">{{ imageInfo?.size || '' }}</n-descriptions-item>
                            <n-descriptions-item label="镜像来源">{{ imageInfo?.image_source || '' }}</n-descriptions-item>
                            <n-descriptions-item label="平台">{{ imageInfo?.os_platform || '' }}</n-descriptions-item>
                            <n-descriptions-item label="更新时间">{{ imageInfo?.updated_at || '' }}</n-descriptions-item>
                            <n-descriptions-item label="开放端口">
                                <n-space>
                                    <span v-for="port in imageInfo?.open_ports || []" :key="port" class="port-tag">{{
                                        port
                                        }}</span>
                                </n-space>
                            </n-descriptions-item>
                        </n-descriptions>

                        <div class="mt-4">
                            <h4>环境变量</h4>
                            <div class="code-block">
                                <pre>{{ (imageInfo?.env_vars || []).join('\n') }}</pre>
                            </div>
                        </div>
                    </template>

                    <div class="mt-4">
                        <h4>Docker 拉取命令</h4>
                        <div class="code-block">
                            <pre>{{ imageInfo?.docker_pull_cmd || '' }}</pre>
                        </div>
                    </div>
                </div>
            </n-spin>
        </n-modal>

        <!-- 轩辕镜像详情弹窗 -->
        <n-modal class="xuanyuan-image-detail-modal" v-model:show="showXuanyuanImageDetail" preset="dialog"
            :close-on-esc="true" :close-on-clickoutside="true" :mask-closable="true"
            :title="isXuanyuanPullCommandMode ? '拉取命令' : '镜像详情'" @close="closeXuanyuanImageDetail"
            style="width: calc(100vw - 400px);">
            <div class="xuanyuan-image-detail-content">
                <template v-if="!isXuanyuanPullCommandMode">
                    <div style="display: flex; align-items: center; margin-bottom: 20px;">
                        <img :src="currentXuanyuanImage?.logo_url?.large || ''" :alt="currentXuanyuanImage?.name || ''"
                            style="width: 64px; height: 64px; border-radius: 8px; margin-right: 16px;" />
                        <div>
                            <h3 style="margin: 0;">{{ currentXuanyuanImage?.name || '' }}</h3>
                            <div style="margin-top: 4px;">
                                <span v-if="currentXuanyuanImage?.badge" class="badge" :style="{
                                    padding: '2px 8px',
                                    borderRadius: '4px',
                                    backgroundColor: currentXuanyuanImage?.badge === 'official' ? '#1890ff' : '#52c41a',
                                    color: 'white',
                                    fontSize: '12px',
                                    marginRight: '8px'
                                }">
                                    {{ currentXuanyuanImage?.badge === 'official' ? '官方' : '认证' }}
                                </span>
                                <span class="publisher">{{ currentXuanyuanImage?.publisher?.name || '' }}</span>
                            </div>
                        </div>
                    </div>

                    <n-descriptions bordered size="small" :column="1">
                        <n-descriptions-item label="镜像名称">{{ currentXuanyuanImage?.name || '' }}</n-descriptions-item>
                        <n-descriptions-item label="短描述">{{ currentXuanyuanImage?.short_description || ''
                            }}</n-descriptions-item>
                        <n-descriptions-item label="点赞数">{{ currentXuanyuanImage?.star_count || 0
                            }}</n-descriptions-item>
                        <n-descriptions-item label="拉取数">{{ currentXuanyuanImage?.pull_count || '0'
                            }}</n-descriptions-item>
                        <n-descriptions-item label="创建时间">{{ currentXuanyuanImage?.created_at || ''
                            }}</n-descriptions-item>
                        <n-descriptions-item label="更新时间">{{ currentXuanyuanImage?.updated_at || ''
                            }}</n-descriptions-item>
                        <n-descriptions-item label="架构">
                            <n-space>
                                <span v-for="(arch, index) in currentXuanyuanImage?.architectures || []" :key="index"
                                    class="arch-tag">
                                    {{ arch.label }}
                                </span>
                            </n-space>
                        </n-descriptions-item>
                        <n-descriptions-item label="操作系统">
                            <n-space>
                                <span v-for="(os, index) in currentXuanyuanImage?.operating_systems || []" :key="index"
                                    class="os-tag">
                                    {{ os.label }}
                                </span>
                            </n-space>
                        </n-descriptions-item>
                        <n-descriptions-item label="媒体类型">{{ currentXuanyuanImage?.media_types?.join(', ') || ''
                            }}</n-descriptions-item>
                        <n-descriptions-item label="内容类型">{{ currentXuanyuanImage?.content_types?.join(', ') || ''
                            }}</n-descriptions-item>
                        <n-descriptions-item label="是否归档">{{ currentXuanyuanImage?.archived ? '是' : '否'
                            }}</n-descriptions-item>
                    </n-descriptions>
                </template>

                <div class="mt-4">
                    <h4>Docker 拉取命令</h4>
                    <div class="code-block">
                        <pre v-if="currentXuanyuanImage?.id">docker pull docker.xuanyuan.me/{{ currentXuanyuanImage?.id }}</pre>
                        <pre v-if="currentXuanyuanImage?.id">docker tag docker.xuanyuan.me/{{ currentXuanyuanImage?.id }} {{ currentXuanyuanImage?.id }}</pre>
                    </div>
                </div>
            </div>
        </n-modal>

        <!-- 镜像标签弹窗 -->
        <n-modal class="image-tags-modal" v-model:show="showImageTagsModal" preset="dialog" :close-on-esc="true"
            :close-on-clickoutside="true" :mask-closable="true" title="镜像标签" @close="closeImageTags"
            style="width: calc(100vw - 400px);">
            <n-spin :show="loadingImageTags">
                <template #description>
                    加载中，请稍后……
                </template>
                <div class="image-tags-content">
                    <h4>{{ currentXuanyuanImage?.name || '' }} 镜像标签</h4>
                    <n-data-table :columns="[
                        { title: '标签名', key: 'name', width: 200 },
                        { title: '架构', key: 'architecture', width: 150 },
                        { title: '操作系统', key: 'os', width: 150 },
                        {
                            title: '大小', key: 'full_size', width: 150, render: (row: any) => {
                                const sizeInMB = (row.full_size / (1024 * 1024)).toFixed(2)
                                return `${sizeInMB} MB`
                            }
                        },
                        { title: '最后推送时间', key: 'last_pushed', width: 200 },
                        { title: '最后拉取时间', key: 'last_pulled', width: 200 },
                        { title: '状态', key: 'status', width: 150 },
                        { title: '标签状态', key: 'tag_status', width: 150 },
                        {
                            title: 'V2 格式', key: 'v2', width: 100, render: (row: any) => {
                                return row.v2 ? '是' : '否'
                            }
                        },
                        { title: '摘要', key: 'digest', width: 400 }
                    ]" :data="imageTags?.results || []" bordered :pagination="{
                            page: 1,
                            pageSize: 20,
                            itemCount: imageTags?.total || 0,
                            showSizePicker: true,
                            pageSizes: [10, 20, 50, 100]
                        }" />
                </div>
            </n-spin>
        </n-modal>
    </div>
</template>

<style lang="scss" scoped>
@use "./index.scss"
</style>