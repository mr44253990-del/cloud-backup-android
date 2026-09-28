package cloud.rakib.backup.data.drive

import android.content.Context
import android.util.Log
import cloud.rakib.backup.data.model.Category
import cloud.rakib.backup.data.model.DriveQuota
import cloud.rakib.backup.data.model.Note
import cloud.rakib.backup.data.model.Todo
import cloud.rakib.backup.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountManager
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayInputStream
import java.io.OutputStream

class DriveService(private val context: Context) {

    private val httpTransport = NetHttpTransport()
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val driveFolder = "CloudBackup"

    private fun getDriveClient(): Drive? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        
        return Drive.Builder(
            httpTransport,
            jsonFactory,
            com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountManager.getDrivingCredential(
                context, account.account, listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA)
            )
        ).setApplicationName("Cloud Backup").build()
    }

    suspend fun getDriveQuota(): DriveQuota? {
        return try {
            val drive = getDriveClient() ?: return null
            val about = drive.about().get().setFields("storageQuota").execute()
            val quota = about.storageQuota
            DriveQuota(
                totalBytes = quota.limitBytes ?: 0,
                usedBytes = quota.usageInDrive ?: 0,
                appUsedBytes = quota.usageAppSpecific ?: 0
            )
        } catch (e: Exception) {
            Log.e("DriveService", "Failed to get drive quota", e)
            null
        }
    }

    private suspend fun getOrCreateBackupFolder(): String? {
        return try {
            val drive = getDriveClient() ?: return null
            
            // Search for existing folder
            val query = "mimeType='application/vnd.google-apps.folder' and trashed=false and name='$driveFolder'"
            val files = drive.files().list()
                .setSpaces("appDataFolder")
                .setQ("$query and '$driveFolder' in parents or name='$driveFolder'")
                .setFields("files(id, name)")
                .execute()
            
            if (!files.files.isEmpty()) {
                files.files[0].id
            } else {
                // Create new folder
                val folderMetadata = File()
                    .setName(driveFolder)
                    .setMimeType("application/vnd.google-apps.folder")
                    .setParents(listOf("appDataFolder"))
                
                val folder = drive.files().create(folderMetadata)
                    .setFields("id")
                    .execute()
                
                folder.id
            }
        } catch (e: Exception) {
            Log.e("DriveService", "Failed to get/create backup folder", e)
            null
        }
    }

    suspend fun backupNote(note: Note): Boolean {
        return try {
            val drive = getDriveClient() ?: return false
            val folderId = getOrCreateBackupFolder() ?: return false
            
            val noteJson = com.google.gson.Gson().toJson(note)
            val inputStream = ByteArrayInputStream(noteJson.toByteArray(Charsets.UTF_8))
            
            val fileMetadata = File()
                .setName("note_${note.id}.json")
                .setParents(listOf(folderId))
                .setMimeType("application/json")
            
            if (note.driveFileId != null) {
                // Update existing file
                drive.files().update(note.driveFileId, fileMetadata, 
                    com.google.api.client.http.FileContent("application/json", createTempFile(noteJson)))
                    .execute()
            } else {
                // Create new file
                drive.files().create(fileMetadata,
                    com.google.api.client.http.FileContent("application/json", createTempFile(noteJson)))
                    .setFields("id")
                    .execute()
            }
            
            true
        } catch (e: Exception) {
            Log.e("DriveService", "Failed to backup note", e)
            false
        }
    }

    suspend fun backupTodo(todo: Todo): Boolean {
        return try {
            val drive = getDriveClient() ?: return false
            val folderId = getOrCreateBackupFolder() ?: return false
            
            val todoJson = com.google.gson.Gson().toJson(todo)
            
            val fileMetadata = File()
                .setName("todo_${todo.id}.json")
                .setParents(listOf(folderId))
                .setMimeType("application/json")
            
            if (todo.driveFileId != null) {
                drive.files().update(todo.driveFileId, fileMetadata,
                    com.google.api.client.http.FileContent("application/json", createTempFile(todoJson)))
                    .execute()
            } else {
                drive.files().create(fileMetadata,
                    com.google.api.client.http.FileContent("application/json", createTempFile(todoJson)))
                    .setFields("id")
                    .execute()
            }
            
            true
        } catch (e: Exception) {
            Log.e("DriveService", "Failed to backup todo", e)
            false
        }
    }

    suspend fun listBackupFiles(): List<BackupFile> {
        return try {
            val drive = getDriveClient() ?: return emptyList()
            val folderId = getOrCreateBackupFolder() ?: return emptyList()
            
            val files = drive.files().list()
                .setSpaces(folderId)
                .setQ("'$folderId' in parents and trashed=false")
                .setFields("files(id, name, size, modifiedTime)")
                .execute()
            
            files.files.map { file ->
                BackupFile(
                    id = file.id,
                    name = file.name,
                    size = file.size?.toLong() ?: 0,
                    modifiedTime = file.modifiedTime?.getValue() ?: 0
                )
            }
        } catch (e: Exception) {
            Log.e("DriveService", "Failed to list backup files", e)
            emptyList()
        }
    }

    private fun createTempFile(content: String): java.io.File {
        val temp = java.io.File.createTempFile("backup_temp", ".json")
        temp.writeText(content)
        return temp
    }

    data class BackupFile(
        val id: String,
        val name: String,
        val size: Long,
        val modifiedTime: Long
    )
}