package cloud.rakib.backup.data

import android.content.Context
import cloud.rakib.backup.CloudBackupApp
import cloud.rakib.backup.data.database.AppDatabase
import cloud.rakib.backup.data.repository.CategoryRepository
import cloud.rakib.backup.data.repository.NoteRepository
import cloud.rakib.backup.data.repository.TodoRepository
import cloud.rakib.backup.data.repository.UserRepository

object RepositoryProvider {
    
    private fun getDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context.applicationContext)
    }
    
    fun provideNoteRepository(context: Context): NoteRepository {
        return NoteRepository(getDatabase(context).noteDao())
    }
    
    fun provideTodoRepository(context: Context): TodoRepository {
        return TodoRepository(getDatabase(context).todoDao())
    }
    
    fun provideCategoryRepository(context: Context): CategoryRepository {
        return CategoryRepository(getDatabase(context).categoryDao())
    }
    
    fun provideUserRepository(context: Context): UserRepository {
        return UserRepository(CloudBackupApp.getPreferences())
    }
}