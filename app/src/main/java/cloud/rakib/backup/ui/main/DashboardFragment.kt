package cloud.rakib.backup.ui.main

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import cloud.rakib.backup.R
import cloud.rakib.backup.CloudBackupApp
import cloud.rakib.backup.data.RepositoryProvider
import cloud.rakib.backup.data.model.DriveQuota
import cloud.rakib.backup.data.model.User
import cloud.rakib.backup.databinding.FragmentDashboardBinding
import cloud.rakib.backup.ui.viewmodel.NoteViewModel
import cloud.rakib.backup.ui.viewmodel.NoteViewModelFactory
import cloud.rakib.backup.ui.viewmodel.TodoViewModel
import cloud.rakib.backup.ui.viewmodel.TodoViewModelFactory
import cloud.rakib.backup.ui.viewmodel.UserViewModel
import cloud.rakib.backup.ui.viewmodel.UserViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val noteViewModel: NoteViewModel by activityViewModels {
        NoteViewModelFactory(RepositoryProvider.provideNoteRepository(requireContext()))
    }
    
    private val todoViewModel: TodoViewModel by activityViewModels {
        TodoViewModelFactory(RepositoryProvider.provideTodoRepository(requireContext()))
    }
    
    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(RepositoryProvider.provideUserRepository(requireContext()))
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeData()
        loadUserAndQuota()
    }
    
    private fun setupUI() {
        binding.apply {
            tvWelcome.text = "Welcome"
            
            btnSyncNow.setOnClickListener {
                // Trigger sync
                syncNow()
            }
        }
    }
    
    private fun observeData() {
        lifecycleScope.launch {
            noteViewModel.allNotes.collectLatest { notes ->
                binding.tvNoteCount.text = notes.size.toString()
                animateCount(binding.tvNoteCount, notes.size)
            }
        }
        
        lifecycleScope.launch {
            todoViewModel.activeTodos.collectLatest { todos ->
                binding.tvTodoCount.text = todos.size.toString()
                animateCount(binding.tvTodoCount, todos.size)
            }
        }
    }
    
    private fun loadUserAndQuota() {
        val user = userViewModel.getCurrentUser()
        if (user != null) {
            binding.tvWelcome.text = "Welcome, ${user.displayName ?: "User"}"
            binding.tvUserEmail.text = user.email
            binding.ivProfilePicture.setImageResource(R.drawable.ic_account_circle_24)
            
            user.driveQuota?.let {
                updateDriveQuota(it)
            }
        } else {
            binding.tvWelcome.text = "Welcome"
            binding.root.findViewById<View>(R.id.tvUserEmail).visibility = View.GONE
        }
    }
    
    private fun updateDriveQuota(quota: DriveQuota) {
        binding.apply {
            tvStorageUsage.text = getString(
                R.string.used_of,
                formatBytes(quota.usedBytes),
                formatBytes(quota.totalBytes)
            )
            
            // Animate storage ring
            val usagePercent = (quota.usagePercentage / 100f) * 360f
            animateRing(progressStorage, usagePercent)
            
            tvUsagePercent.text = "${quota.usagePercentage.toInt()}%"
            
            tvPendingSync.text = "0" // Will be updated from sync status
            
            tvLastBackup.text = formatLastBackup()
            
            // Show storage details
            groupStorageDetails.visibility = View.VISIBLE
            tvOfflineMode.visibility = View.GONE
        }
    }
    
    private fun animateRing(view: View, progress: Float) {
        val animator = ObjectAnimator.ofFloat(view, "progress", 0f, progress)
        animator.duration = 1500
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }
    
    private fun animateCount(textView: android.widget.TextView, target: Int) {
        val animator = ObjectAnimator.ofInt(textView, "text", 0, target)
        animator.duration = 500
        animator.interpolator = DecelerateInterpolator()
        
        // Use custom animator for text
        val startValue = textView.text.toString().toIntOrNull() ?: 0
        val textAnimator = ValueAnimator.ofInt(startValue, target)
        textAnimator.duration = 500
        textAnimator.addUpdateListener { animation ->
            textView.text = animation.animatedValue.toString()
        }
        textAnimator.start()
    }
    
    private fun syncNow() {
        val prefs = CloudBackupApp.getPreferences()
        prefs.edit().putLong("last_backup_time", System.currentTimeMillis()).apply()
        
        val syncIntent = android.content.Intent(requireContext(), cloud.rakib.backup.sync.SyncService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            requireActivity().startForegroundService(syncIntent)
        } else {
            requireActivity().startService(syncIntent)
        }
    }
    
    private fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var unitIndex = 0
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }
        return String.format("%.1f %s", value, units[unitIndex])
    }
    
    private fun formatLastBackup(): String {
        val prefs = CloudBackupApp.getPreferences()
        val lastBackup = prefs.getLong("last_backup_time", 0)
        if (lastBackup == 0L) return getString(R.string.never)
        
        val diff = System.currentTimeMillis() - lastBackup
        return when {
            diff < 60000 -> getString(R.string.just_now)
            diff < 3600000 -> "${diff / 60000} min ago"
            diff < 86400000 -> "${diff / 3600000} hours ago"
            else -> {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                sdf.format(Date(lastBackup))
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = DashboardFragment()
    }
}