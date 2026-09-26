package com.example.data.repository

import com.example.data.db.FieldActivityDao
import com.example.data.model.FieldActivity
import kotlinx.coroutines.flow.Flow

class FieldActivityRepository(private val dao: FieldActivityDao) {
    val allActivities: Flow<List<FieldActivity>> = dao.getAllActivities()

    suspend fun insert(activity: FieldActivity): Long = dao.insertActivity(activity)

    suspend fun update(activity: FieldActivity) = dao.updateActivity(activity)

    suspend fun delete(activity: FieldActivity) = dao.deleteActivity(activity)

    suspend fun updateSentStatus(id: Long, isSent: Boolean) = dao.updateSentStatus(id, isSent)
}
