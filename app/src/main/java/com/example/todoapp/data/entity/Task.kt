package com.example.todoapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val completed: Boolean = false,
    /** Epoch millis; null = sem data de vencimento */
    val dueDateTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val categoryId: Long? = null,
    /**
     * Identificador da notificação local agendada (AlarmManager requestCode).
     * Campo adicional para cancelar/reagendar notificações.
     */
    val notificationId: Int? = null
)
