package com.example.todoapp.data.repository

import com.example.todoapp.data.dao.CategoryDao
import com.example.todoapp.data.dao.TaskDao
import com.example.todoapp.data.entity.Category
import com.example.todoapp.data.entity.Task
import kotlinx.coroutines.flow.Flow

class TodoRepository(
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao
) {
    // --- Tasks ---
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAll()

    fun getFilteredTasks(statusFilter: String, categoryId: Long?): Flow<List<Task>> {
        val hasCategoryFilter = if (categoryId != null) 1 else 0
        val catId = categoryId ?: -1L
        return taskDao.getFiltered(statusFilter, hasCategoryFilter, catId)
    }

    suspend fun getTaskById(id: Long): Task? = taskDao.getById(id)

    suspend fun insertTask(task: Task): Long = taskDao.insert(task)

    suspend fun updateTask(task: Task) = taskDao.update(task)

    suspend fun deleteTask(task: Task) = taskDao.delete(task)

    suspend fun getPendingWithFutureDue(now: Long): List<Task> =
        taskDao.getPendingWithFutureDue(now)

    // --- Categories ---
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAll()

    suspend fun getCategoryById(id: Long): Category? = categoryDao.getById(id)

    suspend fun insertCategory(category: Category): Long = categoryDao.insert(category)

    suspend fun updateCategory(category: Category) = categoryDao.update(category)

    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    suspend fun countTasksUsingCategory(categoryId: Long): Int =
        categoryDao.countTasksUsingCategory(categoryId)
}
