package per.midas.kurumishell.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ssh_connections")
data class SSHConnection(
    @Id
    @Column(length = 36, nullable = false, columnDefinition = "VARCHAR(36) COMMENT '主键UUID'")
    var id: String = UUID.randomUUID().toString(),

    @Column(nullable = false, length = 100, columnDefinition = "VARCHAR(100) COMMENT '连接名称'")
    var name: String,

    @Column(nullable = false, length = 100, columnDefinition = "VARCHAR(100) COMMENT '主机地址'")
    var host: String,

    @Column(nullable = false, columnDefinition = "INT COMMENT '端口号'")
    var port: Int = 22,

    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50) COMMENT '用户名'")
    var username: String,

    @Column(length = 500, columnDefinition = "VARCHAR(500) COMMENT '密码'")
    var password: String? = null,

    @Column(length = 2000, columnDefinition = "VARCHAR(2000) COMMENT '私钥'")
    var privateKey: String? = null,

    @Column(length = 100, columnDefinition = "VARCHAR(100) COMMENT '密钥密码'")
    var keyPassphrase: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", columnDefinition = "VARCHAR(36) COMMENT '所属分组ID'")
    var group: SSHGroup? = null,

    // 用户关联关系，Hibernate会自动从User对象提取id并设置到user_id列
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