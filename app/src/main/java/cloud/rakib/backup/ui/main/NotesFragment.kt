package cloud.rakib.backup.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cloud.rakib.backup.R
import cloud.rakib.backup.data.RepositoryProvider
import cloud.rakib.backup.data.model.Note
import cloud.rakib.backup.databinding.FragmentNotesBinding
import cloud.rakib.backup.ui.adapter.NoteAdapter
import cloud.rakib.backup.ui.viewmodel.NoteViewModel
import cloud.rakib.backup.ui.viewmodel.NoteViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotesFragment : Fragment() {
    
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(RepositoryProvider.provideNoteRepository(requireContext()))
    }
    
    private lateinit var adapter: NoteAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeNotes()
    }
    
    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            onNoteClick = { /* Open note */ },
            onPinClick = { note, isPinned -> 
                viewModel.updateNote(note.copy(isPinned = isPinned)) 
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@NotesFragment.adapter
            setHasFixedSize(true)
        }
    }
    
    private fun observeNotes() {
        lifecycleScope.launch {
            viewModel.allNotes.collectLatest { notes ->
                adapter.submitList(notes)
                binding.tvEmptyState.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
    
    fun search(query: String) {
        if (query.isEmpty()) {
            observeNotes()
        } else {
            lifecycleScope.launch {
                viewModel.searchNotes(query).collectLatest { notes ->
                    adapter.submitList(notes)
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = NotesFragment()
    }
}