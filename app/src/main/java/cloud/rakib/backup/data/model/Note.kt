package cloud.rakib.backup.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val category: String? = null,
    val priority: Priority = Priority.NORMAL,
    val isPinned: Boolean = false,
    val images: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val driveFileId: String? = null
)

enum class Priority(val value: Int, val displayName: String) {
    NORMAL(0, "Normal"),
    IMPORTANT(1, "Important"),
    URGENT(2, "Urgent"),
    EMERGENCY(3, "Emergency")
}
