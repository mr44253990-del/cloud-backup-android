package cloud.rakib.backup.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cloud.rakib.backup.data.model.Subtask
import cloud.rakib.backup.databinding.ItemSubtaskBinding

class SubtaskAdapter(
    private val onDeleteClick: (Subtask) -> Unit
) : ListAdapter<Subtask, SubtaskAdapter.SubtaskViewHolder>(SubtaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskViewHolder {
        val binding = ItemSubtaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SubtaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubtaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubtaskViewHolder(private val binding: ItemSubtaskBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(subtask: Subtask) {
            binding.apply {
                tvTitle.text = subtask.title
                checkBox.isChecked = subtask.isCompleted
                
                checkBox.setOnCheckedChangeListener { _, _ ->
                    // Toggle handled by parent if needed
                }
                
                btnDelete.setOnClickListener {
                    onDeleteClick(subtask)
                }
            }
        }
    }
}

class SubtaskDiffCallback : DiffUtil.ItemCallback<Subtask>() {
    override fun areItemsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Subtask, newItem: Subtask): Boolean {
        return oldItem == newItem
    }
}