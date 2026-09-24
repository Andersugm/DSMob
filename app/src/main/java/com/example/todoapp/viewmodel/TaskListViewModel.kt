package com.example.todoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.TodoApplication
import com.example.todoapp.data.entity.Category
import com.example.todoapp.data.entity.Task
import com.example.todoapp.util.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StatusFilter { ALL, PENDING, COMPLETED }

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val categories: List<Category> = emptyList(),
    val statusFilter: StatusFilter = StatusFilter.ALL,
    val selectedCategoryId: Long? = null,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as TodoApplication).repository
    private val appContext = application.applicationContext

    private val statusFilter = MutableStateFlow(StatusFilter.ALL)
    private val categoryFilter = MutableStateFlow<Long?>(null)

    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<TaskListUiState> = combine(
        statusFilter,
        categoryFilter,
        categories
    ) { status, catId, cats ->
        Triple(status, catId, cats)
    }.flatMapLatest { (status, catId, cats) ->
        val statusStr = when (status) {
            StatusFilter.ALL -> "ALL"
            StatusFilter.PENDING -> "PENDING"
            StatusFilter.COMPLETED -> "COMPLETED"
        }
        repository.getFilteredTasks(statusStr, catId).combine(
            MutableStateFlow(Unit)
        ) { tasks, _ ->
            TaskListUiState(
                tasks = tasks,
                categories = cats,
                statusFilter = status,
                selectedCategoryId = catId,
                isLoading = false
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TaskListUiState()
    )

    fun setStatusFilter(filter: StatusFilter) {
        statusFilter.value = filter
    }

    fun setCategoryFilter(categoryId: Long?) {
        categoryFilter.value = categoryId
    }

    fun toggleCompleted(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(completed = !task.completed)
            if (updated.completed) {
                // Concluiu → cancela notificação
                NotificationHelper.cancel(appContext, task.notificationId)
                repository.updateTask(updated.copy(notificationId = null))
            } else {
                // Reabriu → reagenda se ainda futura
                val newNotifId = NotificationHelper.schedule(appContext, updated)
                repository.updateTask(updated.copy(notificationId = newNotifId))
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            NotificationHelper.cancel(appContext, task.notificationId)
            repository.deleteTask(task)
        }
    }
}
