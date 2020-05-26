package com.wwm.todo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.amazonaws.amplify.generated.graphql.ListTasksQuery

object TodoRepository {

    private val _todoList = MutableLiveData<List<TaskItem>>()
    val todoList: LiveData<List<TaskItem>>
        get() = _todoList

    fun setTodoList(list: List<ListTasksQuery.Item>) {
        val mappedList = mutableListOf<TaskItem>()

        list.forEach {
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

                mappedList.add(item)
            }
        }

        _todoList.postValue(mappedList)
    }


}