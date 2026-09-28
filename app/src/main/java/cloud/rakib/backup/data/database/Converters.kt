package cloud.rakib.backup.data.database

import androidx.room.TypeConverter
import cloud.rakib.backup.data.model.Priority
import cloud.rakib.backup.data.model.Subtask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }
    
    @TypeConverter
    fun fromSubtaskList(value: List<Subtask>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toSubtaskList(value: String): List<Subtask> {
        val type = object : TypeToken<List<Subtask>>() {}.type
        return gson.fromJson(value, type)
    }
    
    @TypeConverter
    fun fromPriority(priority: Priority): Int {
        return priority.value
    }
    
    @TypeConverter
    fun toPriority(value: Int): Priority {
        return Priority.values().find { it.value == value } ?: Priority.NORMAL
    }
}
