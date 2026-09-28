package cloud.rakib.backup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cloud.rakib.backup.data.repository.CategoryRepository
import cloud.rakib.backup.data.repository.NoteRepository
import cloud.rakib.backup.data.repository.TodoRepository
import cloud.rakib.backup.data.repository.UserRepository

class NoteViewModelFactory(
    private val repository: NoteRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NoteViewModel(repository) as T
    }
}

class TodoViewModelFactory(
    private val repository: TodoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TodoViewModel(repository) as T
    }
}

class CategoryViewModelFactory(
    private val repository: CategoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoryViewModel(repository) as T
    }
}

class UserViewModelFactory(
    private val repository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserViewModel(repository) as T
    }
}

class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {
    fun getCurrentUser() = repository.getCurrentUser()
    fun isSignedIn() = repository.isSignedIn()
    fun isCloudMode() = repository.isCloudMode()
    fun clearUser() = repository.clearUser()
    fun saveUser(user: cloud.rakib.backup.data.model.User) = repository.saveUser(user)
    fun saveDriveQuota(quota: cloud.rakib.backup.data.model.DriveQuota) = repository.saveDriveQuota(quota)
}