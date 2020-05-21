package com.wwm.todo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.amazonaws.amplify.generated.graphql.CreateTaskMutation
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.wwm.todo.auth.AuthenticationServiceImpl
import com.wwm.todo.databinding.FragmentAddItemBinding
import type.CreateTaskInput
import javax.annotation.Nonnull

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddItemFragment : Fragment() {

    private lateinit var binding: FragmentAddItemBinding
    private lateinit var viewModel: AddItemViewModel
    private lateinit var mAWSAppSyncClient: AWSAppSyncClient

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

        mAWSAppSyncClient = AWSAppSyncClient.builder()
            .context(requireContext())
            .oidcAuthProvider { AuthenticationServiceImpl.idToken }
            .awsConfiguration(AWSConfiguration(requireContext()))
            // If you are using complex objects (S3) then uncomment
            //.s3ObjectManager(new S3ObjectManagerImplementation(new AmazonS3Client(AWSMobileClient.getInstance())))
            .build()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_second).setOnClickListener {
            // viewModel.addTask(TaskItem(null, binding.titleInput.text.toString(),  binding.descriptionInput.text.toString(),"NOT DONE"))

            val input = CreateTaskInput.builder().title(binding.titleInput.text.toString())
                .description(binding.descriptionInput.text.toString()).status("NOT DONE").build()

            val addTask = CreateTaskMutation.builder().input(input).build()
            mAWSAppSyncClient.mutate(addTask).enqueue(createMutationCallback)

        }
    }

    val createMutationCallback: GraphQLCall.Callback<CreateTaskMutation.Data> =
        object : GraphQLCall.Callback<CreateTaskMutation.Data>() {
            override fun onResponse(@Nonnull response: Response<CreateTaskMutation.Data>) {
                findNavController().navigate(R.id.action_AddItemFragment_to_HomeFragment)
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e("Error", e.toString())
            }
        }
}
