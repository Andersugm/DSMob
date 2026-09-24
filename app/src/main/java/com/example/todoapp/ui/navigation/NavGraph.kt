package com.example.todoapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.todoapp.ui.screens.CategoryScreen
import com.example.todoapp.ui.screens.TaskEditorScreen
import com.example.todoapp.ui.screens.TaskListScreen

object Routes {
    const val TASK_LIST = "task_list"
    const val TASK_EDITOR = "task_editor"
    const val TASK_EDITOR_ARG = "taskId"
    const val CATEGORIES = "categories"
}

@Composable
fun TodoNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.TASK_LIST
    ) {
        composable(Routes.TASK_LIST) {
            TaskListScreen(
                onNavigateToEditor = { taskId ->
                    val route = if (taskId == null) {
                        "${Routes.TASK_EDITOR}/0"
                    } else {
                        "${Routes.TASK_EDITOR}/$taskId"
                    }
                    navController.navigate(route)
                },
                onNavigateToCategories = {
                    navController.navigate(Routes.CATEGORIES)
                }
            )
        }
        composable(
            route = "${Routes.TASK_EDITOR}/{${Routes.TASK_EDITOR_ARG}}",
            arguments = listOf(
                navArgument(Routes.TASK_EDITOR_ARG) {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong(Routes.TASK_EDITOR_ARG) ?: 0L
            TaskEditorScreen(
                taskId = if (taskId == 0L) null else taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.CATEGORIES) {
            CategoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
