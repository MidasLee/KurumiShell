package per.midas.kurumishell.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "markdown_folders")
data class MarkdownFolder(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = "",

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: MarkdownFolder? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "VARCHAR(36) COMMENT '所属用户ID'")
    var user: User? = null,

    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var children: MutableList<MarkdownFolder> = mutableListOf(),

    @OneToMany(mappedBy = "folder", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var notes: MutableList<MarkdownNote> = mutableListOf()
)
