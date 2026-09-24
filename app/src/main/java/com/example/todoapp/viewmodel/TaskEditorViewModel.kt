package com.example.todoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.TodoApplication
import com.example.todoapp.data.entity.Category
import com.example.todoapp.data.entity.Task
import com.example.todoapp.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskEditorUiState(
    val taskId: Long? = null,
    val title: String = "",
    val description: String = "",
    val completed: Boolean = false,
    val dueDateTime: Long? = null,
    val categoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val isNew: Boolean = true,
    val titleError: String? = null,
    val saved: Boolean = false,
    val deleted: Boolean = false
)

class TaskEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as TodoApplication).repository
    private val appContext = application.applicationContext

    private val _uiState = MutableStateFlow(TaskEditorUiState())
    val uiState: StateFlow<TaskEditorUiState> = _uiState

    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadTask(taskId: Long?) {
        viewModelScope.launch {
            val cats = categories.value
            if (taskId == null || taskId == 0L) {
                _uiState.value = TaskEditorUiState(
                    isNew = true,
                    categories = cats
                )
            } else {
                val task = repository.getTaskById(taskId)
                if (task != null) {
                    _uiState.value = TaskEditorUiState(
                        taskId = task.id,
                        title = task.title,
                        description = task.description.orEmpty(),
                        completed = task.completed,
                        dueDateTime = task.dueDateTime,
                        categoryId = task.categoryId,
                        categories = cats,
                        isNew = false
                    )
                }
            }
        }
    }

    fun updateTitle(value: String) {
        _uiState.value = _uiState.value.copy(title = value, titleError = null)
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun updateCompleted(value: Boolean) {
        _uiState.value = _uiState.value.copy(completed = value)
    }

    fun updateDueDateTime(value: Long?) {
        _uiState.value = _uiState.value.copy(dueDateTime = value)
    }

    fun updateCategoryId(value: Long?) {
        _uiState.value = _uiState.value.copy(categoryId = value)
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.value = state.copy(titleError = "Título é obrigatório")
            return
        }
        viewModelScope.launch {
            val existing = state.taskId?.let { repository.getTaskById(it) }
            // Cancela notificação antiga se existir
            existing?.notificationId?.let { NotificationHelper.cancel(appContext, it) }

            val base = Task(
                id = state.taskId ?: 0,
                title = state.title.trim(),
                description = state.description.trim().ifBlank { null },
                completed = state.completed,
                dueDateTime = state.dueDateTime,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                categoryId = state.categoryId,
                notificationId = null
            )

            val id = if (state.isNew) {
                repository.insertTask(base)
            } else {
                repository.updateTask(base)
                base.id
            }

            val withId = base.copy(id = id)
            val notifId = if (!withId.completed && withId.dueDateTime != null &&
                withId.dueDateTime > System.currentTimeMillis()
            ) {
                NotificationHelper.schedule(appContext, withId)
            } else null

            if (notifId != null) {
                repository.updateTask(withId.copy(notificationId = notifId))
            }

            _uiState.value = state.copy(saved = true)
        }
    }

    fun delete() {
        val state = _uiState.value
        val id = state.taskId ?: return
        viewModelScope.launch {
            val task = repository.getTaskById(id) ?: return@launch
            NotificationHelper.cancel(appContext, task.notificationId)
            repository.deleteTask(task)
            _uiState.value = state.copy(deleted = true)
        }
    }
}
