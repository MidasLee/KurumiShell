package per.midas.kurumishell.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "roles")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '主键ID'")
    val id: Long = 0,

    @Column(unique = true, nullable = false, length = 50, columnDefinition = "VARCHAR(50) COMMENT '角色名称'")
    var name: String,

    @Column(length = 200, columnDefinition = "VARCHAR(200) COMMENT '角色描述'")
    var description: String? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_privileges",
        joinColumns = [JoinColumn(name = "role_id")],
        inverseJoinColumns = [JoinColumn(name = "privilege_id")]
    )
    var privileges: MutableSet<Privilege> = HashSet()
) {
    fun addPrivilege(privilege: Privilege) {
        privileges.add(privilege)
        privilege.roles.add(this)
    }

    fun removePrivilege(privilege: Privilege) {
        privileges.remove(privilege)
        privilege.roles.remove(this)
    }
}