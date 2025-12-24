<script setup lang="ts">
import { ref, reactive, h, computed, onMounted } from 'vue'
import {
    NDataTable,
    NSelect,
    NButton,
    NGrid,
    NGi,
    NInputGroup,
    NInputGroupLabel,
    NInput,
    NTag
} from 'naive-ui'
import type { RoleDTO } from '@/dto/RoleDTO.d.ts'
import type { UserDTO } from '@/dto/UserDTO.d.ts'
import { getAllRoles } from '@/pages/UserManagement/service/getAllRoles.ts'
import { getAllUsers } from '@/pages/UserManagement/service/getAllUsers.ts'
import { enableUser } from '@/pages/UserManagement/service/enableUser.ts'
import { disableUser } from '@/pages/UserManagement/service/disableUser.ts'

const searchFormModel = ref({
    id: '',
    username: '',
    email: '',
    enabled: null as boolean | null,
    roles: [] as string[]
})

const enabledStringValue = computed({
    get: () => searchFormModel.value.enabled?.toString() ?? null,
    set: (val) => {
        searchFormModel.value.enabled = val === null ? null : val === 'true'
    }
})

const enabledOptions = ref([
    {
        label: '不限',
        value: 'none'
    },
    {
        label: '启用',
        value: 'true'
    },
    {
        label: '禁用',
        value: 'false'
    }
])

const roleOptions = ref([
    {
        label: '管理员',
        value: 'ROLE_ADMIN'
    },
    {
        label: '用户',
        value: 'ROLE_USER'
    }
])

const userEditButtonClickHandler = (user: UserDTO) => {
    console.log(user)
}

const userToggleStatusButtonClickHandler = async (user: UserDTO) => {
    if (user.enabled) {
        await disableUser(user.id)
    } else {
        await enableUser(user.id)
    }
    await getUserList()
}

const userDeleteButtonClickHandler = (user: UserDTO) => {
    console.log(user)
}

const createColumns = ({
    userEditButtonClickHandler,
    userDeleteButtonClickHandler
}: {
    userEditButtonClickHandler: (row: UserDTO) => void
    userDeleteButtonClickHandler: (row: UserDTO) => void
}) => {
    return [
        {
            title: '工号',
            key: 'id'
        },
        {
            title: '用户名',
            key: 'username'
        },
        {
            title: '邮箱',
            key: 'email'
        },
        {
            title: '启用',
            key: 'enabled',
            render(row: UserDTO) {
                return h(
                    NTag,
                    {
                        type: row.enabled ? 'success' : 'error',  // true显示绿色，false显示红色
                        bordered: false
                    },
                    {
                        default: () => row.enabled ? '是' : '否'
                    }
                )
            }
        },
        {
            title: '角色',
            key: 'role',
            render(row: UserDTO) {
                const tags = row.roles.map((tagKey: RoleDTO) => {
                    return h(
                        NTag,
                        {
                            style: {
                                marginRight: '6px'
                            },
                            type: 'info',
                            bordered: false
                        },
                        {
                            default: () => tagKey.name
                        }
                    )
                })
                return tags
            }
        },
        {
            title: '操作',
            key: 'operation',
            render(row: UserDTO) {
                return h('div', { style: 'display: flex gap: 8px' }, [
                    h(
                        NButton,
                        {
                            size: 'small',
                            type: 'primary',
                            onClick: () => userEditButtonClickHandler(row)
                        },
                        { default: () => '编辑' }
                    ),
                    h(
                        NButton,
                        {
                            size: 'small',
                            type: row.enabled ? 'warning' : 'success', // 禁用状态显示绿色(启用)，启用状态显示黄色(禁用)
                            onClick: () => userToggleStatusButtonClickHandler(row) // 需要添加这个处理函数
                        },
                        { default: () => row.enabled ? '禁用' : '启用' } // 根据状态显示相反操作
                    ),
                    h(
                        NButton,
                        {
                            size: 'small',
                            type: 'error',
                            onClick: () => userDeleteButtonClickHandler(row)
                        },
                        { default: () => '删除' }
                    )
                ])
            }
        }
    ]
}

const userList = ref<UserDTO[]>([])

const paginationReactive = reactive({
    page: 1,
    pageSize: 10,
    showSizePicker: true,
    pageSizes: [10, 20, 30, 40],
    onChange: (page: number) => {
        paginationReactive.page = page
    },
    onUpdatePageSize: (pageSize: number) => {
        paginationReactive.pageSize = pageSize
        paginationReactive.page = 1
    }
})

const searchButtonClickHandler = () => {

}

const resetButtonClickHandler = () => {

}

const getRoleList = async () => {
    const roleRes = await getAllRoles()
    if (roleRes) {
        roleOptions.value = (roleRes.data as unknown as RoleDTO[]).map((role: RoleDTO) => {
            return {
                label: role.name,
                value: role.name
            }
        })
    }
}

const getUserList = async () => {
    const userRes = await getAllUsers(paginationReactive.page - 1, paginationReactive.pageSize)
    if (userRes) {
        const userResData: any = userRes.data
        userList.value = userResData.content
    }
}

onMounted(async () => {
    // 获得角色列表
    await getRoleList()
    // 获得用户列表
    await getUserList()
})
</script>
<template>
    <div class="user-management-div">
        <div class="search-div">
            <n-grid x-gap="16px" :cols="6">
                <n-gi>
                    <n-input-group>
                        <n-input-group-label>工号：</n-input-group-label>
                        <n-input v-model:value="searchFormModel.id" placeholder="请输入工号" />
                    </n-input-group>
                </n-gi>
                <n-gi>
                    <n-input-group>
                        <n-input-group-label>姓名：</n-input-group-label>
                        <n-input v-model:value="searchFormModel.username" placeholder="请输入姓名" />
                    </n-input-group>
                </n-gi>
                <n-gi>
                    <n-input-group>
                        <n-input-group-label>邮箱：</n-input-group-label>
                        <n-input v-model:value="searchFormModel.email" placeholder="请输入邮箱" />
                    </n-input-group>
                </n-gi>
                <n-gi>
                    <n-input-group>
                        <n-input-group-label>启用：</n-input-group-label>
                        <n-select v-model:value="enabledStringValue" placeholder="请选择是否启用" :options="enabledOptions" />
                    </n-input-group>
                </n-gi>
                <n-gi>
                    <n-input-group>
                        <n-input-group-label>角色：</n-input-group-label>
                        <n-select v-model:value="searchFormModel.roles" placeholder="请选择角色" :options="roleOptions"
                            multiple />
                    </n-input-group>
                </n-gi>
                <n-gi class="search-button-gi">
                    <n-button type="primary" ghost @click="searchButtonClickHandler">
                        搜索
                    </n-button>
                    <n-button type="primary" ghost @click="resetButtonClickHandler">
                        重置
                    </n-button>
                </n-gi>
            </n-grid>
        </div>
        <div class="content-div">
            <n-data-table :columns="createColumns({ userEditButtonClickHandler, userDeleteButtonClickHandler })"
                :data="userList" :pagination="paginationReactive" />
        </div>
    </div>
</template>
<style lang="scss" scoped>
@use "./index.scss"
</style>