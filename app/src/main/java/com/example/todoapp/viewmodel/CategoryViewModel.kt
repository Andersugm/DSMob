package com.example.todoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.TodoApplication
import com.example.todoapp.data.entity.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val message: String? = null
)

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as TodoApplication).repository

    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CategoryUiState> = repository.getAllCategories()
        .combineWithMessage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryUiState())

    private fun kotlinx.coroutines.flow.Flow<List<Category>>.combineWithMessage() =
        kotlinx.coroutines.flow.combine(this, _message) { cats, msg ->
            CategoryUiState(categories = cats, message = msg)
        }

    fun clearMessage() {
        _message.value = null
    }

    fun addCategory(name: String, color: Long = 0xFF6200EE) {
        if (name.isBlank()) {
            _message.value = "Nome da categoria é obrigatório"
            return
        }
        viewModelScope.launch {
            repository.insertCategory(Category(name = name.trim(), color = color))
            _message.value = "Categoria criada"
        }
    }

    fun renameCategory(category: Category, newName: String) {
        if (newName.isBlank()) {
            _message.value = "Nome inválido"
            return
        }
        viewModelScope.launch {
            repository.updateCategory(category.copy(name = newName.trim()))
            _message.value = "Categoria renomeada"
        }
    }

    /**
     * Estratégia de exclusão: ForeignKey SET_NULL — tarefas ficam sem categoria.
     * Não bloqueia a exclusão.
     */
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            val count = repository.countTasksUsingCategory(category.id)
            repository.deleteCategory(category)
            _message.value = if (count > 0) {
                "Categoria excluída. $count tarefa(s) ficaram sem categoria."
            } else {
                "Categoria excluída"
            }
        }
    }
}
