package cloud.rakib.backup.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cloud.rakib.backup.data.model.Note
import cloud.rakib.backup.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onPinClick: (Note, Boolean) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(note: Note) {
            binding.apply {
                tvTitle.text = note.title
                tvContent.text = if (note.content.length > 100) {
                    note.content.substring(0, 97) + "..."
                } else {
                    note.content
                }
                tvDate.text = formatDate(note.updatedAt)
                
                // Pin status
                ivPin.setImageResource(
                    if (note.isPinned) cloud.rakib.backup.R.drawable.ic_pin_filled_24
                    else cloud.rakib.backup.R.drawable.ic_pin_outline_24
                )
                
                ivPin.setOnClickListener {
                    onPinClick(note, !note.isPinned)
                }
                
                root.setOnClickListener {
                    onNoteClick(note)
                }
            }
        }
        
        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}

class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }
}