package com.wwm.todo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wwm.todo.databinding.ItemTodoBinding

class TodoListAdapter(
    private var mClickListener: ItemClickedListener,
    private var itemDeleteListener: ItemDeleteListener
) :
    ListAdapter<TaskItem, TodoListAdapter.ItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemTodoBinding.inflate(LayoutInflater.from(parent.context)), itemDeleteListener
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val dayForecast = getItem(position)
        holder.bind(dayForecast)
        holder.itemView.setOnClickListener {
            mClickListener.onClick(dayForecast)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TaskItem>() {
        override fun areItemsTheSame(
            oldItem: TaskItem,
            newItem: TaskItem
        ): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(
            oldItem: TaskItem,
            newItem: TaskItem
        ): Boolean {
            return oldItem == newItem
        }
    }

    class ItemViewHolder(
        private var binding: ItemTodoBinding, private var deleteListener: ItemDeleteListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskItem) {
            binding.item = item
            binding.itemDelete.setOnClickListener { deleteListener.onClick(item) }
            binding.executePendingBindings()
        }
    }
}

class ItemClickedListener(val clickListener: (day: TaskItem) -> Unit) {
    fun onClick(item: TaskItem) = clickListener(item)
}

class ItemDeleteListener(val clickListener: (day: TaskItem) -> Unit) {
    fun onClick(item: TaskItem) = clickListener(item)
}