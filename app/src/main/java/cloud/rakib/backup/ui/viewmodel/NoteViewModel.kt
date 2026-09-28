package cloud.rakib.backup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.rakib.backup.data.model.Note
import cloud.rakib.backup.data.repository.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    val allNotes: StateFlow<List<Note>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pinnedNotes: StateFlow<List<Note>> = repository.pinnedNotes
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getNotesByCategory(category: String) = repository.getNotesByCategory(category)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun searchNotes(query: String) = repository.searchNotes(query)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertNote(note: Note) = viewModelScope.launch {
        repository.insertNote(note)
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        repository.updateNote(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        repository.deleteNote(note)
    }

    fun deleteNoteById(id: String) = viewModelScope.launch {
        repository.deleteNoteById(id)
    }
}