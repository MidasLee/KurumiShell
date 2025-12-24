package per.midas.kurumishell.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "privileges")
data class Privilege(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT COMMENT '主键ID'")
    val id: Long = 0,

    @Column(unique = true, nullable = false, length = 50, columnDefinition = "VARCHAR(50) COMMENT '权限名称'")
    var name: String,

    @Column(length = 200, columnDefinition = "VARCHAR(200) COMMENT '权限描述'")
    var description: String? = null,

    @ManyToMany(mappedBy = "privileges", fetch = FetchType.LAZY)
    val roles: MutableSet<Role> = mutableSetOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Privilege) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}