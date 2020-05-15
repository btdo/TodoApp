package com.wwm.todo

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("todoList")
fun bindTodoList(
    recyclerView: RecyclerView,
    data: List<TaskItem>?
) {
    val adapter = recyclerView.adapter as TodoListAdapter
    adapter.submitList(data)
}