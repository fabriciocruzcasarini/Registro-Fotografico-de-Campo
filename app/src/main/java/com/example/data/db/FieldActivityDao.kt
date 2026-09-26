package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FieldActivity
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldActivityDao {
    @Query("SELECT * FROM field_activities ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<FieldActivity>>

    @Query("SELECT * FROM field_activities WHERE id = :id")
    suspend fun getActivityById(id: Long): FieldActivity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: FieldActivity): Long

    @Update
    suspend fun updateActivity(activity: FieldActivity)

    @Delete
    suspend fun deleteActivity(activity: FieldActivity)

    @Query("UPDATE field_activities SET isSent = :isSent WHERE id = :id")
    suspend fun updateSentStatus(id: Long, isSent: Boolean)
}
