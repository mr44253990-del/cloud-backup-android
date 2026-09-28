package cloud.rakib.backup.data.repository

import cloud.rakib.backup.data.database.TodoDao
import cloud.rakib.backup.data.model.Todo
import kotlinx.coroutines.flow.Flow

class TodoRepository(
    private val todoDao: TodoDao
) {
    val activeTodos: Flow<List<Todo>> = todoDao.getActiveTodos()
    val completedTodos: Flow<List<Todo>> = todoDao.getCompletedTodos()
    val overdueTodos: Flow<List<Todo>> = todoDao.getOverdueTodos()

    fun getTodosByCategory(category: String): Flow<List<Todo>> = todoDao.getTodosByCategory(category)

    fun searchTodos(query: String): Flow<List<Todo>> = todoDao.searchTodos(query)

    suspend fun getTodoById(id: String): Todo? = todoDao.getTodoById(id)

    suspend fun insertTodo(todo: Todo) = todoDao.insertTodo(todo)

    suspend fun updateTodo(todo: Todo) = todoDao.updateTodo(todo)

    suspend fun deleteTodo(todo: Todo) = todoDao.deleteTodo(todo)

    suspend fun deleteTodoById(id: String) = todoDao.deleteTodoById(id)

    suspend fun getUnsyncedTodos(): List<Todo> = todoDao.getUnsyncedTodos()

    suspend fun markAsSynced(todoId: String, driveFileId: String) = todoDao.markAsSynced(todoId, driveFileId)
}