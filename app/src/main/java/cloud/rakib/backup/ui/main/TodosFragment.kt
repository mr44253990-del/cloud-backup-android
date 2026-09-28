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
import cloud.rakib.backup.data.model.Todo
import cloud.rakib.backup.databinding.FragmentTodosBinding
import cloud.rakib.backup.ui.adapter.TodoAdapter
import cloud.rakib.backup.ui.viewmodel.TodoViewModel
import cloud.rakib.backup.ui.viewmodel.TodoViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TodosFragment : Fragment() {
    
    private var _binding: FragmentTodosBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: TodoViewModel by activityViewModels {
        TodoViewModelFactory(RepositoryProvider.provideTodoRepository(requireContext()))
    }
    
    private lateinit var adapter: TodoAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodosBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeTodos()
    }
    
    private fun setupRecyclerView() {
        adapter = TodoAdapter(
            onTodoClick = { /* Open todo */ },
            onToggleComplete = { todo, isCompleted ->
                viewModel.updateTodo(todo.copy(isCompleted = isCompleted, completedAt = if (isCompleted) System.currentTimeMillis() else null))
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TodosFragment.adapter
            setHasFixedSize(true)
        }
    }
    
    private fun observeTodos() {
        lifecycleScope.launch {
            viewModel.activeTodos.collectLatest { todos ->
                adapter.submitList(todos)
                binding.tvEmptyState.visibility = if (todos.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
    
    fun search(query: String) {
        if (query.isEmpty()) {
            observeTodos()
        } else {
            lifecycleScope.launch {
                viewModel.searchTodos(query).collectLatest { todos ->
                    adapter.submitList(todos)
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = TodosFragment()
    }
}