
import { RoleDTO } from '@/dto/RoleDTO'

export type UserDTO = {
    id: string
    username: string
    email: string
    enabled: boolean | null
    roles: RoleDTO[]
}