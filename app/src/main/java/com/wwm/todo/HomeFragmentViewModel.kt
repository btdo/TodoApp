package com.wwm.todo

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amazonaws.amplify.generated.graphql.ListTasksQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class HomeFragmentViewModel : ViewModel() {

    val todoList: LiveData<List<TaskItem>> = TodoRepository.todoList

    init {
    }

    fun onUpdatedList(list: List<ListTasksQuery.Item>) {
        TodoRepository.setTodoList(list)
    }
}

class HomeFragmentViewModelFactory :
    ViewModelProvider.Factory {
    @ExperimentalCoroutinesApi
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeFragmentViewModel::class.java)) {
            return HomeFragmentViewModel() as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}