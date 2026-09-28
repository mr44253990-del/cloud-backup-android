package cloud.rakib.backup.data.database

import androidx.room.*
import cloud.rakib.backup.data.model.Todo
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    
    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY isPinned DESC, dueDate ASC, updatedAt DESC")
    fun getActiveTodos(): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTodos(): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE id = :todoId")
    suspend fun getTodoById(todoId: String): Todo?
    
    @Query("SELECT * FROM todos WHERE category = :category ORDER BY isPinned DESC, dueDate ASC, updatedAt DESC")
    fun getTodosByCategory(category: String): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchTodos(query: String): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE dueDate IS NOT NULL AND dueDate < :currentTime AND isCompleted = 0")
    fun getOverdueTodos(currentTime: Long = System.currentTimeMillis()): Flow<List<Todo>>
    
    @Query("SELECT * FROM todos WHERE isSynced = 0")
    suspend fun getUnsyncedTodos(): List<Todo>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: Todo)
    
    @Update
    suspend fun updateTodo(todo: Todo)
    
    @Delete
    suspend fun deleteTodo(todo: Todo)
    
    @Query("DELETE FROM todos WHERE id = :todoId")
    suspend fun deleteTodoById(todoId: String)
    
    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 0")
    fun getActiveTodosCount(): Flow<Int>
    
    @Query("UPDATE todos SET isSynced = 1, driveFileId = :driveFileId WHERE id = :todoId")
    suspend fun markAsSynced(todoId: String, driveFileId: String)
}
