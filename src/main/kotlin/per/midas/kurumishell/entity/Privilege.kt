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
    val id: Long = 0,

    @Column(unique = true, nullable = false, length = 50)
    var name: String,

    @Column(length = 200)
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