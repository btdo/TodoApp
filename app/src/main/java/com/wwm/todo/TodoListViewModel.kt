package com.wwm.todo

import android.app.Application
import androidx.lifecycle.*

class TodoListViewModel(application: Application) : AndroidViewModel(application) {

    private val _todoList = MutableLiveData<List<TodoItem>>().apply {

    }
    val todoList: LiveData<List<TodoItem>>
        get() = _todoList

    init {
        _todoList.value =   mutableListOf<TodoItem>(TodoItem("121","Laundry", false))
    }
}

class TodoListViewModelFactory(
    private val application: Application
) :
    ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoListViewModel::class.java)) {
            return TodoListViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}