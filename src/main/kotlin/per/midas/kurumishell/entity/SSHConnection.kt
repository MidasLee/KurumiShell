package per.midas.kurumishell.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ssh_connections")
data class SSHConnection(
    @Id
    @Column(length = 36, nullable = false)
    var id: String = UUID.randomUUID().toString(),

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, length = 100)
    var host: String,

    @Column(nullable = false)
    var port: Int = 22,

    @Column(nullable = false, length = 50)
    var username: String,

    @Column(length = 500)
    var password: String? = null,

    @Column(length = 2000)
    var privateKey: String? = null,

    @Column(length = 100)
    var keyPassphrase: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: SSHGroup? = null,

    // 用户关联关系，Hibernate会自动从User对象提取id并设置到user_id列
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
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

    fun assignToGroup(newGroup: SSHGroup?) {
        this.group?.removeConnection(this)
        this.group = newGroup
        if (newGroup != null) {
            this.user = newGroup.user
        }
        newGroup?.addConnection(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SSHConnection
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}