package cloud.rakib.backup.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: String,
    val icon: String? = null,
    val noteCount: Int = 0,
    val todoCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
