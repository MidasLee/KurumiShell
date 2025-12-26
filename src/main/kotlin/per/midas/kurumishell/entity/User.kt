package per.midas.kurumishell.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    @Column(length = 36)
    val id: String = UUID.randomUUID().toString(),

    @Column(unique = false, nullable = false, length = 50)
    var username: String,

    @Column(nullable = false, length = 100)
    var password: String,

    @Column(unique = true, nullable = false, length = 100)
    var email: String,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: MutableSet<Role> = HashSet(),

    @Column(nullable = false)
    var enabled: Boolean = true,

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: Instant = Instant.now(),

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var sshConnections: MutableList<SSHConnection> = ArrayList(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var sshGroups: MutableList<SSHGroup> = ArrayList()
) {
    @PrePersist
    fun onPersist() {
        createdAt = Instant.now()
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }

    fun addRole(role: Role) {
        roles.add(role)
    }

    fun removeRole(role: Role) {
        roles.remove(role)
    }

    fun addSshConnection(connection: SSHConnection) {
        if (sshConnections.add(connection)) {
            connection.user = this
        }
    }

    fun removeSshConnection(connection: SSHConnection) {
        if (sshConnections.remove(connection)) {
            connection.user = null
        }
    }

    fun addSshGroup(group: SSHGroup) {
        if (sshGroups.add(group)) {
            group.user = this
        }
    }

    fun removeSshGroup(group: SSHGroup) {
        if (sshGroups.remove(group)) {
            group.user = null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as User
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}