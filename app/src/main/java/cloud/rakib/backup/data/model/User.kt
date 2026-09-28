package cloud.rakib.backup.data.model

data class User(
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val googleId: String,
    val accessToken: String?,
    val driveQuota: DriveQuota? = null
)

data class DriveQuota(
    val totalBytes: Long,
    val usedBytes: Long,
    val appUsedBytes: Long
) {
    val freeBytes: Long
        get() = totalBytes - usedBytes
    
    val usagePercentage: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()) * 100 else 0f
    
    val appUsagePercentage: Float
        get() = if (totalBytes > 0) (appUsedBytes.toFloat() / totalBytes.toFloat()) * 100 else 0f
}
