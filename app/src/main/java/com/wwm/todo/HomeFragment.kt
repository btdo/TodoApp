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
import com.amazonaws.amplify.generated.graphql.DeleteTaskMutation
import com.amazonaws.amplify.generated.graphql.ListTasksQuery
import com.amazonaws.amplify.generated.graphql.OnCreateTaskSubscription
import com.amazonaws.amplify.generated.graphql.OnDeleteTaskSubscription
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.wwm.todo.auth.AuthenticationServiceImpl
import com.wwm.todo.databinding.FragmentHomeBinding
import timber.log.Timber
import type.DeleteTaskInput
import javax.annotation.Nonnull


/**
 * A simple [HomeFragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeFragmentViewModel
    private lateinit var mAWSAppSyncClient: AWSAppSyncClient

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val viewModelFactory = HomeFragmentViewModelFactory()
        binding = FragmentHomeBinding.inflate(inflater)
        binding.lifecycleOwner = this
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HomeFragmentViewModel::class.java)
        binding.viewModel = viewModel

        val adapter = TodoListAdapter(ItemClickedListener { }, ItemDeleteListener { item ->
            deleteTask(item)
        })
        binding.itemList.adapter =  adapter
        // Inflate the layout for this fragment

        mAWSAppSyncClient = AWSAppSyncClient.builder()
            .context(requireActivity())
            .oidcAuthProvider { AuthenticationServiceImpl.idToken }
            .awsConfiguration( AWSConfiguration(requireContext()))
            // If you are using complex objects (S3) then uncomment
            //.s3ObjectManager(new S3ObjectManagerImplementation(new AmazonS3Client(AWSMobileClient.getInstance())))
            .build()

        return binding.root
    }

    fun deleteTask(item: TaskItem) {
        val delete = DeleteTaskInput.builder().id(item.id)._version(item._version).build()
        mAWSAppSyncClient.mutate(DeleteTaskMutation.builder().input(delete).build())
            .enqueue(deleteMutationCallback)
    }

    private lateinit var onCreateSubscriptionWatcher: AppSyncSubscriptionCall<OnCreateTaskSubscription.Data>
    private lateinit var onDeleteTaskSubscriptionWatcher: AppSyncSubscriptionCall<OnDeleteTaskSubscription.Data>

    private fun subscribeOnCreate() {
        val subscription: OnCreateTaskSubscription = OnCreateTaskSubscription.builder().build()
        onCreateSubscriptionWatcher = mAWSAppSyncClient.subscribe(subscription)
        onCreateSubscriptionWatcher.execute(onCreateSubscriptionCallback)
    }

    private val onCreateSubscriptionCallback: AppSyncSubscriptionCall.Callback<OnCreateTaskSubscription.Data> =
        object : AppSyncSubscriptionCall.Callback<OnCreateTaskSubscription.Data> {
            override fun onResponse(response: Response<OnCreateTaskSubscription.Data?>) {
                Log.i("Subscription", response.data().toString())
                query()
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e("Subscription", e.toString())
            }

            override fun onCompleted() {
                Log.i("Subscription", "Subscription completed")
            }
        }

    private fun subscribeOnDelete() {
        val subscription: OnDeleteTaskSubscription = OnDeleteTaskSubscription.builder().build()
        onDeleteTaskSubscriptionWatcher = mAWSAppSyncClient.subscribe(subscription)
        onDeleteTaskSubscriptionWatcher.execute(onDeleteSubscriptionCallback)
    }

    val onDeleteSubscriptionCallback: AppSyncSubscriptionCall.Callback<OnDeleteTaskSubscription.Data> =
        object : AppSyncSubscriptionCall.Callback<OnDeleteTaskSubscription.Data> {
            override fun onResponse(response: Response<OnDeleteTaskSubscription.Data?>) {
                Log.i("Subscription", response.data().toString())
                query()
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e("Subscription", e.toString())
            }

            override fun onCompleted() {
                Log.i("Subscription", "Subscription completed")
            }
        }

    val deleteMutationCallback: GraphQLCall.Callback<DeleteTaskMutation.Data> =
        object : GraphQLCall.Callback<DeleteTaskMutation.Data>() {
            override fun onResponse(@Nonnull response: Response<DeleteTaskMutation.Data>) {
                query()
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e("Error", e.toString())
            }
        }

    fun query() {
        mAWSAppSyncClient.query(ListTasksQuery.builder().build())
            .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
            .enqueue(listCallback)
    }

    val listCallback: GraphQLCall.Callback<ListTasksQuery.Data> =
        object : GraphQLCall.Callback<ListTasksQuery.Data>() {
            override fun onResponse(response: Response<ListTasksQuery.Data>) {
                Log.i("Results", response.data()?.listTasks()?.items().toString())
                viewModel.onUpdatedList(response.data()?.listTasks()?.items()!!)
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Timber.e(e, "ERROR")
            }
        }


    override fun onResume() {
        super.onResume()
        query()
        subscribeOnCreate()
        subscribeOnDelete()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_AddItemFragment)
        }
    }

    override fun onStop() {
        super.onStop()
        onCreateSubscriptionWatcher.cancel()
        onDeleteTaskSubscriptionWatcher.let {
            it.cancel()
        }
    }
}
