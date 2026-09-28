package cloud.rakib.backup.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import cloud.rakib.backup.R
import cloud.rakib.backup.CloudBackupApp
import cloud.rakib.backup.data.RepositoryProvider
import cloud.rakib.backup.databinding.ActivityMainBinding
import cloud.rakib.backup.ui.settings.SettingsActivity
import cloud.rakib.backup.ui.viewmodel.UserViewModel
import cloud.rakib.backup.ui.viewmodel.UserViewModelFactory

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(RepositoryProvider.provideUserRepository(this))
    }
    
    private var currentFragment: Fragment? = null
    private var searchView: SearchView? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user should start at onboarding
        val prefs = CloudBackupApp.getPreferences()
        if (!prefs.getBoolean("first_launch_done", false)) {
            startActivity(Intent(this, cloud.rakib.backup.ui.onboarding.OnboardingActivity::class.java))
            finish()
            return
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupNavigation()
        setupFAB()
        setupDrawer()
        
        // Set default fragment (Dashboard)
        if (savedInstanceState == null) {
            binding.navView.setNavigationItemSelectedListener { false } // Disable default behavior
            replaceFragment(DashboardFragment.newInstance(), "dashboard")
            binding.navView.menu.getItem(0).isChecked = true
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
    }
    
    private fun setupNavigation() {
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    replaceFragment(DashboardFragment.newInstance(), "dashboard")
                    supportActionBar?.title = getString(R.string.dashboard)
                    true
                }
                R.id.nav_notes -> {
                    replaceFragment(NotesFragment.newInstance(), "notes")
                    supportActionBar?.title = getString(R.string.notes)
                    true
                }
                R.id.nav_todos -> {
                    replaceFragment(TodosFragment.newInstance(), "todos")
                    supportActionBar?.title = getString(R.string.todos)
                    true
                }
                R.id.nav_categories -> {
                    replaceFragment(CategoriesFragment.newInstance(), "categories")
                    supportActionBar?.title = getString(R.string.categories)
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupFAB() {
        binding.fab.setOnClickListener {
            when (currentFragment) {
                is NotesFragment -> addNote()
                is TodosFragment -> addTodo()
                is CategoriesFragment -> addCategory()
                else -> {}
            }
        }
    }
    
    private fun setupDrawer() {
        val headerView = binding.navView.getHeaderView(0)
        val tvUserName = headerView.findViewById<android.widget.TextView>(R.id.tvUserName)
        val tvUserEmail = headerView.findViewById<android.widget.TextView>(R.id.tvUserEmail)
        
        val user = userViewModel.getCurrentUser()
        if (user != null) {
            tvUserName.text = user.displayName ?: "User"
            tvUserEmail.text = user.email
        } else {
            tvUserName.text = "Guest"
            tvUserEmail.text = if (userViewModel.isCloudMode()) "Cloud Mode" else "Offline Mode"
        }
    }
    
    private fun replaceFragment(fragment: Fragment, tag: String) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .commit()
        
        // Update FAB visibility based on fragment
        when (fragment) {
            is NotesFragment, is TodosFragment, is CategoriesFragment -> binding.fab.show()
            else -> binding.fab.hide()
        }
        
        // Update search menu visibility
        invalidateOptionsMenu()
    }
    
    private fun addNote() {
        val intent = Intent(this, cloud.rakib.backup.ui.editor.NoteEditorActivity::class.java)
        startActivity(intent)
    }
    
    private fun addTodo() {
        val intent = Intent(this, cloud.rakib.backup.ui.editor.TodoEditorActivity::class.java)
        startActivity(intent)
    }
    
    private fun addCategory() {
        // Show add category dialog
        val dialog = cloud.rakib.backup.ui.editor.AddCategoryDialogFragment()
        dialog.show(supportFragmentManager, "add_category")
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        
        // Show search only for notes and todos
        searchItem.isVisible = currentFragment is NotesFragment || currentFragment is TodosFragment
        
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            
            override fun onQueryTextChange(newText: String?): Boolean {
                when (val fragment = currentFragment) {
                    is NotesFragment -> fragment.search(newText ?: "")
                    is TodosFragment -> fragment.search(newText ?: "")
                }
                return true
            }
        })
        
        return true
    }
    
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                }
                true
            }
            R.id.action_sync -> {
                // Trigger sync
                val syncIntent = Intent(this, cloud.rakib.backup.sync.SyncService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(syncIntent)
                } else {
                    startService(syncIntent)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (currentFragment is DashboardFragment) {
                super.onBackPressed()
            } else {
                replaceFragment(DashboardFragment.newInstance(), "dashboard")
                binding.navView.menu.getItem(0).isChecked = true
            }
        }
    }
    
    fun updateFABIcon(iconRes: Int) {
        binding.fab.setImageResource(iconRes)
    }
}