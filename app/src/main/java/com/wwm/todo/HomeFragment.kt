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
        val application = requireActivity().application
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
            .oidcAuthProvider { "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IllvQXA3WmR5YktDT3NJa2JxYnlwZyJ9.eyJpc3MiOiJodHRwczovL2Rldi1iaWRmbG9xOC5hdXRoMC5jb20vIiwic3ViIjoiYXV0aDB8NWVjMGFiYmQ0MGZhNTYwYzc1NjMxMTQxIiwiYXVkIjoidko2emJpMFNkc2lKNTJzZVd3RGYwVDBONnhqZllmbTYiLCJpYXQiOjE1ODk5MzYwNzQsImV4cCI6MTU4OTk3MjA3NCwibm9uY2UiOiJmVnlncEduWXU5TUxpX0ViaXlkVmtVWFlaNGYybVRsTG5FdVZQLTZFUlpJIn0.W-ItRKPJef8DU-UsYwNQ3eZQpdsz4C7316gzM_dbPMAydYSUDe3T54uI68zfp91IdccABHBuXFVist9beXQoixUWG0FzII2mCeyX7ZgIZYjHxQ5I2oQMxH46sKaDlvmS-EtelVOqdNMwEVzstW5Fs7FolIp9Hi555IXcsD5k77yeZ2TAX6dLjm-Z53vXYdfnyoYbobcSsX6kj0fthSCh6pV5QuqxcM-p224YDmDzIhBgTihrRjP5TsgmCVJaWDkeQx5RrRq3bqXCoF8-bBjymhw5IFPpkBxsngCsVn-gYwtnSKWxo3N5o1ouhjThCGc_j0yIQnzBSM_By8eIdiTuYQ"}
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
            .enqueue(todosCallback)
    }

    private val todosCallback: GraphQLCall.Callback<ListTasksQuery.Data> =
        object : GraphQLCall.Callback<ListTasksQuery.Data>() {
            override fun onResponse(response: com.apollographql.apollo.api.Response<ListTasksQuery.Data>) {
                Log.i("Results", response.data()?.listTasks()?.items().toString())
            }

            override fun onFailure(@Nonnull e: ApolloException) {
                Log.e("ERROR", e.toString())
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_AddItemFragment)
        }
    }
}
