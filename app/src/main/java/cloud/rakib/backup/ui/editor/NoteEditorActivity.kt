package cloud.rakib.backup.ui.editor

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cloud.rakib.backup.R
import cloud.rakib.backup.CloudBackupApp
import cloud.rakib.backup.data.RepositoryProvider
import cloud.rakib.backup.data.model.Note
import cloud.rakib.backup.data.model.Priority
import cloud.rakib.backup.databinding.ActivityNoteEditorBinding
import cloud.rakib.backup.ui.viewmodel.NoteViewModel
import cloud.rakib.backup.ui.viewmodel.NoteViewModelFactory
import cloud.rakib.backup.util.ImagePicker

class NoteEditorActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityNoteEditorBinding
    private val viewModel: NoteViewModel by viewModels {
        NoteViewModelFactory(RepositoryProvider.provideNoteRepository(this))
    }
    
    private var note: Note? = null
    private var isNewNote = true
    
    private val categoryAdapter = mutableListOf<String>()
    private var selectedPriority = Priority.NORMAL
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityNoteEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Get note ID from intent if editing
        val noteId = intent.getStringExtra("note_id")
        if (noteId != null) {
            isNewNote = false
            loadNote(noteId)
        }
        
        setupUI()
    }
    
    private fun loadNote(noteId: String) {
        lifecycleScope.launchWhenResumed {
            viewModel.allNotes.collect { notes ->
                notes.find { it.id == noteId }?.let {
                    note = it
                    populateNote(it)
                }
            }
        }
    }
    
    private fun populateNote(note: Note) {
        binding.apply {
            etTitle.setText(note.title)
            etContent.setText(note.content)
            etTags.setText(note.tags.joinToString(", "))
            selectedPriority = note.priority
            
            // Set priority chip
            updatePriorityChip()
        }
    }
    
    private fun setupUI() {
        binding.apply {
            btnSave.setOnClickListener {
                saveNote()
            }
            
            btnAddImage.setOnClickListener {
                pickImage()
            }
            
            btnPriority.setOnClickListener {
                showPriorityDialog()
            }
            
            // Setup category dropdown
            val prefs = CloudBackupApp.getPreferences()
            val savedCategories = prefs.getStringSet("categories", setOf()) ?: setOf()
            categoryAdapter.addAll(savedCategories.toList())
            
            val adapter = ArrayAdapter(
                this@NoteEditorActivity,
                android.R.layout.simple_dropdown_item_1line,
                categoryAdapter
            )
            autoCompleteCategory.setAdapter(adapter)
        }
    }
    
    private fun updatePriorityChip() {
        binding.chipPriority.text = selectedPriority.displayName
        binding.chipPriority.setChipBackgroundColorResource(
            when (selectedPriority) {
                Priority.NORMAL -> R.color.priority_normal
                Priority.IMPORTANT -> R.color.priority_important
                Priority.URGENT -> R.color.priority_urgent
                Priority.EMERGENCY -> R.color.priority_emergency
            }
        )
    }
    
    private fun showPriorityDialog() {
        val priorities = Priority.values()
        val names = priorities.map { it.displayName }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle(R.string.priority)
            .setItems(names) { _, which ->
                selectedPriority = priorities[which]
                updatePriorityChip()
            }
            .show()
    }
    
    private fun saveNote() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }
        
        val tags = binding.etTags.text.toString().trim()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        
        val category = binding.autoCompleteCategory.text.toString().trim().takeIf { it.isNotEmpty() }
        
        val noteToSave = note?.copy(
            title = title,
            content = content,
            category = category,
            priority = selectedPriority,
            tags = tags,
            updatedAt = System.currentTimeMillis(),
            isSynced = false
        ) ?: Note(
            title = title,
            content = content,
            category = category,
            priority = selectedPriority,
            tags = tags
        )
        
        viewModel.insertNote(noteToSave)
        
        // Save category if new
        category?.let {
            val prefs = CloudBackupApp.getPreferences()
            val categories = prefs.getStringSet("categories", mutableSetOf())?.toMutableSet()
            categories?.add(it)
            prefs.edit().putStringSet("categories", categories).apply()
        }
        
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun pickImage() {
        ImagePicker.pick(this)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                note?.let {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.confirm_delete)
                        .setMessage(R.string.confirm_delete_note)
                        .setPositiveButton(R.string.delete) { _, _ ->
                            viewModel.deleteNote(it)
                            Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                } ?: run {
                    Toast.makeText(this, "Nothing to delete", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_share -> {
                val content = binding.etContent.text.toString()
                if (content.isNotEmpty()) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type("text/plain")
                        putExtra(Intent.EXTRA_TEXT, content)
                    }
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
                }
                true
            }
            R.id.action_pin -> {
                note?.let {
                    val updated = it.copy(isPinned = !it.isPinned)
                    viewModel.updateNote(updated)
                    note = updated
                    Toast.makeText(this, if (updated.isPinned) "Pinned" else "Unpinned", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!isNewNote) {
            menuInflater.inflate(R.menu.menu_note_editor, menu)
        }
        return true
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}