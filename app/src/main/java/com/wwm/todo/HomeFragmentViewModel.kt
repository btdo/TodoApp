package com.wwm.todo

import android.app.Application
import androidx.lifecycle.*
import com.wwm.todo.auth.AuthenticationServiceImpl
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalCoroutinesApi
class HomeFragmentViewModel : ViewModel(){
    private val handler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
    }

    private val _todoList = MutableLiveData<List<TaskItem>>()
    val todoList: LiveData<List<TaskItem>>
        get() = _todoList

    init {
        // login()
    }

    private fun login() {
        viewModelScope.launch(handler) {
            AuthenticationServiceImpl.login("sbstg2napp", "Happy123!")
            Timber.d("${AuthenticationServiceImpl.accessToken} ${AuthenticationServiceImpl.refreshToken}")
        }
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