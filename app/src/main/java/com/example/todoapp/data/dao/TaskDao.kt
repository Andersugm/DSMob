package com.example.todoapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.todoapp.data.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query(
        """
        SELECT * FROM tasks
        ORDER BY completed ASC,
                 CASE WHEN dueDateTime IS NULL THEN 1 ELSE 0 END ASC,
                 dueDateTime ASC,
                 createdAt DESC
        """
    )
    fun getAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): Task?

    @Query(
        """
        SELECT * FROM tasks
        WHERE (:statusFilter = 'ALL'
               OR (:statusFilter = 'PENDING' AND completed = 0)
               OR (:statusFilter = 'COMPLETED' AND completed = 1))
          AND (:hasCategoryFilter = 0 OR categoryId = :categoryId)
        ORDER BY completed ASC,
                 CASE WHEN dueDateTime IS NULL THEN 1 ELSE 0 END ASC,
                 dueDateTime ASC,
                 createdAt DESC
        """
    )
    fun getFiltered(
        statusFilter: String,
        hasCategoryFilter: Int,
        categoryId: Long
    ): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query(
        """
        SELECT * FROM tasks
        WHERE dueDateTime IS NOT NULL
          AND completed = 0
          AND dueDateTime > :now
        """
    )
    suspend fun getPendingWithFutureDue(now: Long): List<Task>
}
