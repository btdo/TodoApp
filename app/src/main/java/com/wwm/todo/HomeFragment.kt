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
import com.amazonaws.Response
import com.amazonaws.amplify.generated.graphql.ListTasksQuery
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.exception.ApolloException
import com.wwm.todo.auth.AuthenticationServiceImpl
import com.wwm.todo.databinding.FragmentHomeBinding
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

        val adapter =  TodoListAdapter(ItemClickedListener {  })
        binding.itemList.adapter =  adapter
        // Inflate the layout for this fragment

        mAWSAppSyncClient = AWSAppSyncClient.builder()
            .context(requireContext())
            .oidcAuthProvider { AuthenticationServiceImpl.idToken }
            .awsConfiguration( AWSConfiguration(requireContext()))
            // If you are using complex objects (S3) then uncomment
            //.s3ObjectManager(new S3ObjectManagerImplementation(new AmazonS3Client(AWSMobileClient.getInstance())))
            .build();

        query()

        return binding.root
    }

    fun query() {
        mAWSAppSyncClient.query(ListTasksQuery.builder().build())
            .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
            .enqueue(viewModel.todosCallback)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_AddItemFragment)
        }
    }
}
