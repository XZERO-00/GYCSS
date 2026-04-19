package com.gycss.app.ui.volunteer.help

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.gycss.app.databinding.ActivityAvailableRequestsBinding
import com.gycss.app.ui.chat.ChatActivity
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

        setupUI()
        setupRecyclerView()
        setupObservers()
        
        viewModel.fetchAvailableRequests()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = HelpRequestAdapter { request ->
            // UX Improvement: Snackbar with undo and immediate feedback
            val snackbar = Snackbar.make(binding.root, "Accepting ${request.title}...", Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction("CANCEL") {
                // If the user cancels, we could potentially stop the VM call if it hasn't finished
            }
            snackbar.show()
            
            // Visual feedback: Dim the list while processing
            binding.recyclerView.alpha = 0.5f
            binding.recyclerView.isEnabled = false
            
            viewModel.acceptRequest(request)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

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
            binding.recyclerView.alpha = 1.0f
            binding.recyclerView.isEnabled = true
            
            result.onSuccess {
                Toast.makeText(this, "Request Accepted Successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure {
                Toast.makeText(this, "Failed to accept: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
