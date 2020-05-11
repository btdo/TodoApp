package com.wwm.todo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wwm.todo.databinding.ItemTodoBinding

class TodoListAdapter(
    private var mClickListener: ItemClickedListener
) :
    ListAdapter<TodoItem, TodoListAdapter.ItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemTodoBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val dayForecast = getItem(position)
        holder.bind(dayForecast)
        holder.itemView.setOnClickListener {
            mClickListener.onClick(dayForecast)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TodoItem>() {
        override fun areItemsTheSame(
            oldItem: TodoItem,
            newItem: TodoItem
        ): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(
            oldItem: TodoItem,
            newItem: TodoItem
        ): Boolean {
            return oldItem == newItem
        }
    }

    class ItemViewHolder(
        private var binding: ItemTodoBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TodoItem) {
            binding.item = item
            binding.executePendingBindings()
        }
    }
}

class ItemClickedListener(val clickListener: (day: TodoItem) -> Unit) {
    fun onClick(item: TodoItem) = clickListener(item)
}