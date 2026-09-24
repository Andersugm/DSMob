package com.example.todoapp.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todoapp.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Após reinício do dispositivo, reagenda notificações de tarefas pendentes com dueDateTime futuro.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val tasks = db.taskDao().getPendingWithFutureDue(System.currentTimeMillis())
                tasks.forEach { task ->
                    NotificationHelper.schedule(context, task)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
