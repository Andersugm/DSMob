package com.example.todoapp

import android.app.Application
import com.example.todoapp.data.AppDatabase
import com.example.todoapp.data.repository.TodoRepository
import com.example.todoapp.util.NotificationHelper

class TodoApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy {
        TodoRepository(database.taskDao(), database.categoryDao())
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
