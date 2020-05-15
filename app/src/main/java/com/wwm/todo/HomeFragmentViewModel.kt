package com.wwm.todo

import android.app.Application
import androidx.lifecycle.*

class HomeFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val _todoList = MutableLiveData<List<TaskItem>>()
    val todoList: LiveData<List<TaskItem>>
        get() = _todoList

    init {

    }


}

class HomeFragmentViewModelFactory(
    private val application: Application
) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeFragmentViewModel::class.java)) {
            return HomeFragmentViewModel(application) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}