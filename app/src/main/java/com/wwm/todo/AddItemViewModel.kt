package com.wwm.todo

import android.app.Application
import androidx.lifecycle.*

class AddItemViewModel(application: Application) : AndroidViewModel(application)  {

    fun addTask(task: TaskItem){

    }
}

class AddItemViewModelFactory(
    private val application: Application
) :
    ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddItemViewModel::class.java)) {
            return AddItemViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}