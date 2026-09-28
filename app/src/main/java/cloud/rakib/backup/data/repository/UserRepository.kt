package cloud.rakib.backup.data.repository

import android.content.SharedPreferences
import cloud.rakib.backup.data.model.DriveQuota
import cloud.rakib.backup.data.model.User

class UserRepository(
    private val preferences: SharedPreferences
) {
    fun getCurrentUser(): User? {
        val isSignedIn = preferences.getBoolean("is_signed_in", false)
        if (!isSignedIn) return null
        
        val email = preferences.getString("user_email", "") ?: ""
        val displayName = preferences.getString("user_name", null)
        val photoUrl = preferences.getString("user_photo", null)
        val googleId = preferences.getString("google_id", "") ?: ""
        val accessToken = preferences.getString("google_access_token", null)
        
        val driveQuota = if (preferences.contains("drive_total_bytes")) {
            DriveQuota(
                totalBytes = preferences.getLong("drive_total_bytes", 0),
                usedBytes = preferences.getLong("drive_used_bytes", 0),
                appUsedBytes = preferences.getLong("drive_app_used_bytes", 0)
            )
        } else null
        
        return if (email.isNotEmpty() && googleId.isNotEmpty()) {
            User(email, displayName, photoUrl, googleId, accessToken, driveQuota)
        } else null
    }

    fun saveUser(user: User) {
        preferences.edit().apply {
            putBoolean("is_signed_in", true)
            putString("user_email", user.email)
            putString("user_name", user.displayName)
            putString("user_photo", user.photoUrl)
            putString("google_id", user.googleId)
            putString("google_access_token", user.accessToken)
            user.driveQuota?.let {
                putLong("drive_total_bytes", it.totalBytes)
                putLong("drive_used_bytes", it.usedBytes)
                putLong("drive_app_used_bytes", it.appUsedBytes)
            }
            apply()
        }
    }

    fun saveDriveQuota(quota: DriveQuota) {
        preferences.edit().apply {
            putLong("drive_total_bytes", quota.totalBytes)
            putLong("drive_used_bytes", quota.usedBytes)
            putLong("drive_app_used_bytes", quota.appUsedBytes)
            apply()
        }
    }

    fun clearUser() {
        preferences.edit().apply {
            remove("is_signed_in")
            remove("user_email")
            remove("user_name")
            remove("user_photo")
            remove("google_id")
            remove("google_access_token")
            apply()
        }
    }

    fun isSignedIn(): Boolean = preferences.getBoolean("is_signed_in", false)

    fun isCloudMode(): Boolean = preferences.getBoolean("is_cloud_mode", false)
}