package com.gycss.app.ui.volunteer.help

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gycss.app.databinding.ActivityAvailableRequestsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AvailableRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAvailableRequestsBinding
    private val viewModel: VolunteerHelpViewModel by viewModels()
    private lateinit var adapter: HelpRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvailableRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        
        viewModel.fetchAvailableRequests()
    }

    private fun setupRecyclerView() {
        adapter = HelpRequestAdapter { requestId ->
            viewModel.acceptRequest(requestId)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.availableRequests.observe(this) { requests ->
            if (requests.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.submitList(requests)
            }
        }

        viewModel.acceptResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Request Accepted!", Toast.LENGTH_SHORT).show()
                // Navigate to Chat or Active Tasks
            }.onFailure {
                Toast.makeText(this, "Failed to accept: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
