package per.midas.kurumishell.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ssh_groups", uniqueConstraints = [UniqueConstraint(columnNames = ["name", "user_id"])])
data class SSHGroup(
    @Id
    @Column(length = 36, nullable = false, columnDefinition = "VARCHAR(36) COMMENT '主键UUID'")
    var id: String = UUID.randomUUID().toString(),

    @Column(nullable = false, length = 100, columnDefinition = "VARCHAR(100) COMMENT '分组名称'")
    var name: String,

    @Column(length = 500, columnDefinition = "VARCHAR(500) COMMENT '分组描述'")
    var description: String? = null,

    @OneToMany(mappedBy = "group", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var connections: MutableList<SSHConnection> = ArrayList(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "VARCHAR(36) COMMENT '所属用户ID'")
    var user: User? = null,

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP COMMENT '创建时间'")
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP COMMENT '更新时间'")
    var updatedAt: Instant? = null
) {
    @PrePersist
    fun onPersist() {
        createdAt = Instant.now()
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }

    fun addConnection(connection: SSHConnection) {
        if (connections.add(connection)) {
            connection.group = this
        }
    }

    fun removeConnection(connection: SSHConnection) {
        if (connections.remove(connection)) {
            connection.group = null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SSHGroup
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}