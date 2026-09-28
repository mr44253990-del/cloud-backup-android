package cloud.rakib.backup.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val category: String? = null,
    val priority: Priority = Priority.NORMAL,
    val isPinned: Boolean = false,
    val isCompleted: Boolean = false,
    val dueDate: Long? = null,
    val reminderTime: Long? = null,
    val subtasks: List<Subtask> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isSynced: Boolean = false,
    val driveFileId: String? = null
)

data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)

val Todo.status: TodoStatus
    get() = when {
        isCompleted -> TodoStatus.COMPLETED
        dueDate != null && dueDate < System.currentTimeMillis() -> TodoStatus.OVERDUE
        else -> TodoStatus.PENDING
    }

enum class TodoStatus(val displayName: String) {
    PENDING("Pending"),
    COMPLETED("Completed"),
    OVERDUE("Overdue")
}
