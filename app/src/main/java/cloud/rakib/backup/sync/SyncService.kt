package cloud.rakib.backup.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import cloud.rakib.backup.R
import cloud.rakib.backup.data.RepositoryProvider
import cloud.rakib.backup.data.drive.DriveService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SyncService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    
    private lateinit var driveService: DriveService
    private lateinit var noteRepository: cloud.rakib.backup.data.repository.NoteRepository
    private lateinit var todoRepository: cloud.rakib.backup.data.repository.TodoRepository

    override fun onCreate() {
        super.onCreate()
        
        val context = applicationContext
        driveService = DriveService(context)
        noteRepository = RepositoryProvider.provideNoteRepository(context)
        todoRepository = RepositoryProvider.provideTodoRepository(context)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        
        val notification = createNotification()
        startForeground(1, notification)

        serviceScope.launch {
            syncData()
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private suspend fun syncData() {
        try {
            val notes = noteRepository.allNotes.first()
            val todos = todoRepository.activeTodos.first()
            
            Log.d("SyncService", "Syncing ${notes.size} notes and ${todos.size} todos")
            
            // Backup notes
            notes.filter { !it.isSynced }.forEach { note ->
                val success = driveService.backupNote(note)
                if (success) {
                    noteRepository.markAsSynced(note.id, "temp_file_id")
                }
            }
            
            // Backup todos
            todos.filter { !it.isSynced }.forEach { todo ->
                val success = driveService.backupTodo(todo)
                if (success) {
                    todoRepository.markAsSynced(todo.id, "temp_file_id")
                }
            }
            
        } catch (e: Exception) {
            Log.e("SyncService", "Sync failed", e)
        }
    }

    private fun createNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "sync_channel")
                .setContentTitle("Syncing...")
                .setContentText("Backing up your data")
                .setSmallIcon(R.drawable.ic_sync_24)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("Syncing...")
                .setContentText("Backing up your data")
                .setSmallIcon(R.drawable.ic_sync_24)
                .build()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sync_channel",
                "Sync Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}