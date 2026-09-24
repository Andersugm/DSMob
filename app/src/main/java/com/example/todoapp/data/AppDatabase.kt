package com.example.todoapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.todoapp.data.dao.CategoryDao
import com.example.todoapp.data.dao.TaskDao
import com.example.todoapp.data.entity.Category
import com.example.todoapp.data.entity.Task

@Database(
    entities = [Task::class, Category::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "todo_app.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed via SQL direto para evitar race com INSTANCE
                            db.execSQL("INSERT INTO categories (name, color) VALUES ('Pessoal', ${0xFF4CAF50})")
                            db.execSQL("INSERT INTO categories (name, color) VALUES ('Trabalho', ${0xFF2196F3})")
                            db.execSQL("INSERT INTO categories (name, color) VALUES ('Estudo', ${0xFFFF9800})")
                            db.execSQL("INSERT INTO categories (name, color) VALUES ('Compras', ${0xFFE91E63})")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
