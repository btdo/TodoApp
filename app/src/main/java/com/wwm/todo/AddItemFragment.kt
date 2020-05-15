package com.wwm.todo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.wwm.todo.databinding.FragmentAddItemBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddItemFragment : Fragment() {

    private lateinit var binding: FragmentAddItemBinding
    private lateinit var viewModel: AddItemViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val application = requireActivity().application
        val viewModelFactory = AddItemViewModelFactory(application)
        binding = FragmentAddItemBinding.inflate(inflater)
        binding.lifecycleOwner = this
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AddItemViewModel::class.java)
        binding.viewModel = viewModel
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_second).setOnClickListener {
            viewModel.addTask(TaskItem(null, binding.titleInput.text.toString(),  binding.descriptionInput.text.toString(),"NOT DONE"))
            findNavController().navigate(R.id.action_AddItemFragment_to_HomeFragment)
        }
    }
}
