package com.wwm.todo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amazonaws.amplify.generated.graphql.ListTasksQuery
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.exception.ApolloException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.annotation.Nonnull


@ExperimentalCoroutinesApi
class HomeFragmentViewModel : ViewModel() {

    private val handler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
    }

    private val _todoList = MutableLiveData<List<TaskItem>>()
    val todoList: LiveData<List<TaskItem>>
        get() = _todoList

    init {
    }

    val listCallback: GraphQLCall.Callback<ListTasksQuery.Data> =
        object : GraphQLCall.Callback<ListTasksQuery.Data>() {
            override fun onResponse(response: com.apollographql.apollo.api.Response<ListTasksQuery.Data>) {
                Log.i("Results", response.data()?.listTasks()?.items().toString())
                val list = mutableListOf<TaskItem>()
                response.data()?.listTasks()?.items().let {
                    it?.forEach {
                        val deleted = it._deleted() ?: false
                        if (!deleted) {
                            val item = TaskItem(
                                it.id(),
                                it.title(),
                                it.description(),
                                it.status(),
                                it._version(),
                                it._deleted()
                            )
                            list.add(item)
                        }
                    }
                }

                _todoList.postValue(list)
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e("ERROR", e.toString())
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