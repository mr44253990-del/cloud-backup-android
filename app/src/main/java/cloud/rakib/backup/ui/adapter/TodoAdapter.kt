package cloud.rakib.backup.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cloud.rakib.backup.data.model.Todo
import cloud.rakib.backup.data.model.status
import cloud.rakib.backup.databinding.ItemTodoBinding
import java.text.SimpleDateFormat
import java.util.*

class TodoAdapter(
    private val onTodoClick: (Todo) -> Unit,
    private val onToggleComplete: (Todo, Boolean) -> Unit
) : ListAdapter<Todo, TodoAdapter.TodoViewHolder>(TodoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ItemTodoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TodoViewHolder(private val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(todo: Todo) {
            binding.apply {
                tvTitle.text = todo.title
                tvDescription.text = todo.description ?: ""
                tvStatus.text = todo.status.displayName
                tvDate.text = todo.dueDate?.let { formatDate(it) } ?: ""
                
                checkBox.isChecked = todo.isCompleted
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    onToggleComplete(todo, isChecked)
                }
                
                root.setOnClickListener {
                    onTodoClick(todo)
                }
            }
        }
        
        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}

class TodoDiffCallback : DiffUtil.ItemCallback<Todo>() {
    override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
        return oldItem == newItem
    }
}