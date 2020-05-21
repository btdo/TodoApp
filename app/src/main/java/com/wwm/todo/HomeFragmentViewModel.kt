package com.wwm.todo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class HomeFragmentViewModel : ViewModel() {

    private val _todoList = MutableLiveData<List<TaskItem>>()
    val todoList: LiveData<List<TaskItem>>
        get() = _todoList

    init {
    }

    fun onUpdatedList(list: List<TaskItem>) {
        _todoList.postValue(list)
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