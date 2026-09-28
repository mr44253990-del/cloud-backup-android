package cloud.rakib.backup.ui.editor

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cloud.rakib.backup.R
import cloud.rakib.backup.data.RepositoryProvider
import cloud.rakib.backup.data.model.Priority
import cloud.rakib.backup.data.model.Subtask
import cloud.rakib.backup.data.model.Todo
import cloud.rakib.backup.databinding.ActivityTodoEditorBinding
import cloud.rakib.backup.ui.viewmodel.TodoViewModel
import cloud.rakib.backup.ui.viewmodel.TodoViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class TodoEditorActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTodoEditorBinding
    private val viewModel: TodoViewModel by viewModels {
        TodoViewModelFactory(RepositoryProvider.provideTodoRepository(this))
    }
    
    private var todo: Todo? = null
    private var isNewTodo = true
    private val subtasks = mutableListOf<Subtask>()
    private lateinit var subtaskAdapter: SubtaskAdapter
    private var selectedPriority = Priority.NORMAL
    private var dueDate: Long? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityTodoEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Get todo ID from intent if editing
        val todoId = intent.getStringExtra("todo_id")
        if (todoId != null) {
            isNewTodo = false
            loadTodo(todoId)
        }
        
        setupRecyclerView()
        setupUI()
    }
    
    private fun loadTodo(todoId: String) {
        lifecycleScope.launchWhenResumed {
            viewModel.activeTodos.collect { todos ->
                todos.find { it.id == todoId }?.let {
                    todo = it
                    populateTodo(it)
                }
            }
        }
    }
    
    private fun populateTodo(todo: Todo) {
        binding.apply {
            etTitle.setText(todo.title)
            etDescription.setText(todo.description ?: "")
            etTags.setText(todo.tags.joinToString(", "))
            selectedPriority = todo.priority
            dueDate = todo.dueDate
            
            updatePriorityChip()
            updateDueDateDisplay()
            
            subtasks.clear()
            subtasks.addAll(todo.subtasks)
            subtaskAdapter.submitList(subtasks.toList())
        }
    }
    
    private fun setupRecyclerView() {
        subtaskAdapter = SubtaskAdapter(
            onDeleteClick = { subtask ->
                subtasks.remove(subtask)
                subtaskAdapter.submitList(subtasks.toList())
            }
        )
        
        binding.recyclerViewSubtasks.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@TodoEditorActivity)
            adapter = subtaskAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupUI() {
        binding.apply {
            btnSave.setOnClickListener {
                saveTodo()
            }
            
            btnPriority.setOnClickListener {
                showPriorityDialog()
            }
            
            btnDueDate.setOnClickListener {
                showDatePicker()
            }
            
            fabAddSubtask.setOnClickListener {
                val subtaskTitle = etSubtask.text.toString().trim()
                if (subtaskTitle.isNotEmpty()) {
                    val subtask = Subtask(title = subtaskTitle)
                    subtasks.add(subtask)
                    subtaskAdapter.submitList(subtasks.toList())
                    etSubtask.text?.clear()
                    Toast.makeText(this@TodoEditorActivity, "Subtask added", Toast.LENGTH_SHORT).show()
                }
            }
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
    
    private fun updateDueDateDisplay() {
        binding.tvDueDate.text = dueDate?.let { formatDate(it) } ?: "Set due date"
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
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        dueDate?.let { calendar.timeInMillis = it }
        
        val datePicker = androidx.appcompat.widget.DatePicker(
            this, null
        )
        datePicker.updateDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePicker.setSpinnerTextAppearance(R.style.TextBody)
        
        AlertDialog.Builder(this)
            .setTitle(R.string.due_date)
            .setView(datePicker)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val selected = Calendar.getInstance().apply {
                    set(datePicker.year, datePicker.month, datePicker.dayOfMonth, 0, 0)
                }
                dueDate = selected.timeInMillis
                updateDueDateDisplay()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    private fun saveTodo() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }
        
        val tags = binding.etTags.text.toString().trim()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        
        val todoToSave = todo?.copy(
            title = title,
            description = description.ifEmpty { null },
            priority = selectedPriority,
            dueDate = dueDate,
            tags = tags,
            subtasks = subtasks.toList(),
            updatedAt = System.currentTimeMillis(),
            isSynced = false
        ) ?: Todo(
            title = title,
            description = description.ifEmpty { null },
            priority = selectedPriority,
            dueDate = dueDate,
            tags = tags,
            subtasks = subtasks.toList()
        )
        
        viewModel.insertTodo(todoToSave)
        
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                todo?.let {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.confirm_delete)
                        .setMessage(R.string.confirm_delete_todo)
                        .setPositiveButton(R.string.delete) { _, _ ->
                            viewModel.deleteTodo(it)
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
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!isNewTodo) {
            menuInflater.inflate(R.menu.menu_todo_editor, menu)
        }
        return true
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}