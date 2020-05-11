package com.wwm.todo

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("todoList")
fun bindDailyForecastRecyclerView(
    recyclerView: RecyclerView,
    data: List<TodoItem>?
) {
    val adapter = recyclerView.adapter as TodoListAdapter
    adapter.submitList(data)
}