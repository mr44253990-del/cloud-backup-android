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
import cloud.rakib.backup.data.model.Category
import cloud.rakib.backup.databinding.FragmentCategoriesBinding
import cloud.rakib.backup.ui.adapter.CategoryAdapter
import cloud.rakib.backup.ui.viewmodel.CategoryViewModel
import cloud.rakib.backup.ui.viewmodel.CategoryViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoriesFragment : Fragment() {
    
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CategoryViewModel by activityViewModels {
        CategoryViewModelFactory(RepositoryProvider.provideCategoryRepository(requireContext()))
    }
    
    private lateinit var adapter: CategoryAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeCategories()
    }
    
    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            onCategoryClick = { /* Handle category click */ }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CategoriesFragment.adapter
            setHasFixedSize(true)
        }
    }
    
    private fun observeCategories() {
        lifecycleScope.launch {
            viewModel.allCategories.collectLatest { categories ->
                adapter.submitList(categories)
                binding.tvEmptyState.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = CategoriesFragment()
    }
}