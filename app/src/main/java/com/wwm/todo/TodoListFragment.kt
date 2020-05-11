package com.wwm.todo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.wwm.todo.databinding.FragmentTodoListBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class TodoListFragment : Fragment() {

    private lateinit var binding: FragmentTodoListBinding
    private lateinit var viewModel: TodoListViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val application = requireActivity().application
        val viewModelFactory = TodoListViewModelFactory(application)
        binding = FragmentTodoListBinding.inflate(inflater)
        binding.lifecycleOwner = this
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TodoListViewModel::class.java)
        binding.viewModel = viewModel

        val adapter =  TodoListAdapter(ItemClickedListener {  })
        binding.itemList.adapter =  adapter
        // Inflate the layout for this fragment
        return binding.root
    }
}
