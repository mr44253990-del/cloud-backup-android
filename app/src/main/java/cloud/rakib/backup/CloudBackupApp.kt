package cloud.rakib.backup

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class CloudBackupApp : Application() {
    
    companion object {
        private lateinit var instance: CloudBackupApp
        
        fun getAppContext(): Context = instance.applicationContext
        
        fun getPreferences(): SharedPreferences {
            return instance.getSharedPreferences("cloud_backup_prefs", Context.MODE_PRIVATE)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize app components
        initializeDatabase()
        initializePreferences()
    }
    
    private fun initializeDatabase() {
        // Database initialization will be done lazily
    }
    
    private fun initializePreferences() {
        val prefs = getPreferences()
        
        // Set default values if first launch
        if (!prefs.contains("first_launch_done")) {
            prefs.edit().apply {
                putBoolean("first_launch_done", false)
                putBoolean("is_cloud_mode", false)
                putString("theme", "system")
                putString("accent_color", "primary")
                putBoolean("auto_backup", true)
                putBoolean("wifi_only", true)
                putString("backup_frequency", "daily")
                apply()
            }
        }
    }
}
