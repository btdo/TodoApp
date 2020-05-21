package com.wwm.todo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.amazonaws.amplify.generated.graphql.CreateTaskMutation
import com.amazonaws.amplify.generated.graphql.ListTasksQuery
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.ClearCacheOptions
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.wwm.todo.auth.AuthenticationServiceImpl
import com.wwm.todo.databinding.FragmentAddItemBinding
import timber.log.Timber
import type.CreateTaskInput
import java.util.*
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
                .description(binding.descriptionInput.text.toString()).build()

            val addTask = CreateTaskMutation.builder().input(input).build()
            if (isConnectedToInternet()) {
                mAWSAppSyncClient.mutate(addTask).enqueue(createMutationCallback)
            } else {
                mAWSAppSyncClient.mutate(addTask).enqueue(createMutationCallbackOffline)
            }

            optimisticWrite(input)
        }
    }

    val createMutationCallback: GraphQLCall.Callback<CreateTaskMutation.Data> =
        object : GraphQLCall.Callback<CreateTaskMutation.Data>() {
            override fun onResponse(@Nonnull response: Response<CreateTaskMutation.Data>) {
                hideKeyboard()
                findNavController().navigate(R.id.action_AddItemFragment_to_HomeFragment)
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Timber.e(e, "Error creating tasks")
            }
        }

    val createMutationCallbackOffline: GraphQLCall.Callback<CreateTaskMutation.Data> =
        object : GraphQLCall.Callback<CreateTaskMutation.Data>() {
            override fun onResponse(@Nonnull response: Response<CreateTaskMutation.Data>) {}

            override fun onFailure(@Nonnull e: ApolloException) {
                Timber.e(e, "Error creating tasks")
            }

            override fun onStatusEvent(event: GraphQLCall.StatusEvent) {
                super.onStatusEvent(event)
                if (!isConnectedToInternet()) {
                    // Need to do this to avoid duplicates
                    mAWSAppSyncClient.clearCaches(
                        ClearCacheOptions.builder().clearMutations().build()
                    )
                }
            }
        }

    private fun optimisticWrite(createTodoInput: CreateTaskInput) {
        val expected: CreateTaskMutation.CreateTask = CreateTaskMutation.CreateTask(
            "Task",  //GraphQL Type name
            UUID.randomUUID().toString(),
            createTodoInput.title(),
            createTodoInput.description(),
            null,
            1,
            false,
            System.currentTimeMillis()
        )

        val listTasksQuery = ListTasksQuery.builder().build()

        mAWSAppSyncClient.query(listTasksQuery)
            .responseFetcher(AppSyncResponseFetchers.CACHE_ONLY)
            .enqueue(object : GraphQLCall.Callback<ListTasksQuery.Data>() {
                override fun onResponse(response: com.apollographql.apollo.api.Response<ListTasksQuery.Data>) {
                    //Populate a copy of the query in the cache
                    val items = mutableListOf<ListTasksQuery.Item>()
                    response.data()?.listTasks()?.items().let {
                        items.addAll(it!!)
                    }

                    //Add the newly created item to the cache copy
                    items.add(
                        ListTasksQuery.Item(
                            expected.__typename(),
                            expected.id(),
                            expected.title(),
                            expected.description(),
                            expected.status(),
                            expected._version(),
                            expected._deleted(),
                            expected._lastChangedAt()
                        )
                    )

                    //Overwrite the cache with the new results
                    val data: ListTasksQuery.Data = ListTasksQuery.Data(
                        ListTasksQuery.ListTasks(
                            "ModelTaskConnection", items, null, null
                        )
                    )

                    mAWSAppSyncClient.store
                        .write(
                            listTasksQuery,
                            data
                        ).enqueue(null)
                    // Successful writing to local store
                    finishIfOffline()
                }

                override fun onFailure(@Nonnull e: ApolloException) {
                    Log.e("ERROR", e.toString())
                }
            })
    }

    private fun finishIfOffline() {
        // Close the add activity when offline otherwise allow callback to close
        if (!isConnectedToInternet()) {
            hideKeyboard()
            findNavController().navigate(R.id.action_AddItemFragment_to_HomeFragment)
        }
    }

    fun isConnectedToInternet(): Boolean {
        try {
            val cm =
                requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
            return isConnected
        } catch (e: Exception) {
            return false
        }
    }

    private fun hideKeyboard() {
        requireActivity().let {
            val inputMethodManager = it.getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            it.currentFocus?.let { currentFocus ->
                inputMethodManager.hideSoftInputFromWindow(currentFocus.applicationWindowToken, 0)
            }
        }
    }
}
