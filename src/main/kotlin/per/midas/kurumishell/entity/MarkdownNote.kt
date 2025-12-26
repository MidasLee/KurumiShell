package per.midas.kurumishell.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "markdown_notes")
data class MarkdownNote(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String = "",

    @Column(nullable = false)
    var title: String = "",

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    var content: String = "",

    @ElementCollection
    @CollectionTable(name = "markdown_note_tags", joinColumns = [JoinColumn(name = "note_id")])
    @Column(name = "tag")
    var tags: MutableList<String> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    var folder: MarkdownFolder? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
