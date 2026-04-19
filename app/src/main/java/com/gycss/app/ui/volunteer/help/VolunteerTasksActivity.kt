package com.gycss.app.ui.volunteer.help

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.gycss.app.databinding.ActivityVolunteerTasksBinding
import com.gycss.app.ui.chat.ChatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VolunteerTasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteerTasksBinding
    private val viewModel: VolunteerTasksViewModel by viewModels()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupTabs()
        setupObservers()

        // Include CompletedByVolunteer in the active tasks list
        viewModel.fetchTasks(listOf("Accepted", "In Progress", "CompletedByVolunteer"))
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onChatClick = { viewModel.getOrCreateChat(it) },
            onCompleteClick = { viewModel.completeTask(it.requestId) }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.fetchTasks(listOf("Accepted", "In Progress", "CompletedByVolunteer"))
                    1 -> viewModel.fetchTasks(listOf("Completed"))
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupObservers() {
        viewModel.tasks.observe(this) { tasks ->
            if (tasks.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvTasks.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvTasks.visibility = View.VISIBLE
                adapter.submitList(tasks)
            }
        }

        viewModel.chatId.observe(this) { chatId ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("CHAT_ID", chatId)
            }
            startActivity(intent)
        }
    }
}
