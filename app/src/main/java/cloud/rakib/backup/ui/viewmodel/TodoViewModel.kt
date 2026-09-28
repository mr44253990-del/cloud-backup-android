package cloud.rakib.backup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.rakib.backup.data.model.Todo
import cloud.rakib.backup.data.repository.TodoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(
    private val repository: TodoRepository
) : ViewModel() {

    val activeTodos: StateFlow<List<Todo>> = repository.activeTodos
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val completedTodos: StateFlow<List<Todo>> = repository.completedTodos
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val overdueTodos: StateFlow<List<Todo>> = repository.overdueTodos
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getTodosByCategory(category: String) = repository.getTodosByCategory(category)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun searchTodos(query: String) = repository.searchTodos(query)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertTodo(todo: Todo) = viewModelScope.launch {
        repository.insertTodo(todo)
    }

    fun updateTodo(todo: Todo) = viewModelScope.launch {
        repository.updateTodo(todo)
    }

    fun deleteTodo(todo: Todo) = viewModelScope.launch {
        repository.deleteTodo(todo)
    }

    fun deleteTodoById(id: String) = viewModelScope.launch {
        repository.deleteTodoById(id)
    }
}