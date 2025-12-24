import type { PrivilegeDTO } from './PrivilegeDTO'
export type RoleDTO = {
    id: string
    name: string
    description: string
    privileges: PrivilegeDTO[]
}