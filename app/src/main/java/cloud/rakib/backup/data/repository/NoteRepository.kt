package cloud.rakib.backup.data.repository

import cloud.rakib.backup.data.database.NoteDao
import cloud.rakib.backup.data.model.Note
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao
) {
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()
    val pinnedNotes: Flow<List<Note>> = noteDao.getPinnedNotes()

    fun getNotesByCategory(category: String): Flow<List<Note>> = noteDao.getNotesByCategory(category)

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    suspend fun getNoteById(id: String): Note? = noteDao.getNoteById(id)

    suspend fun insertNote(note: Note) = noteDao.insertNote(note)

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    suspend fun deleteNoteById(id: String) = noteDao.deleteNoteById(id)

    suspend fun getUnsyncedNotes(): List<Note> = noteDao.getUnsyncedNotes()

    suspend fun markAsSynced(noteId: String, driveFileId: String) = noteDao.markAsSynced(noteId, driveFileId)
}