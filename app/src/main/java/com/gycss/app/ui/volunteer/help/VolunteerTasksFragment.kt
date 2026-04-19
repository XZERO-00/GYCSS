package com.gycss.app.ui.volunteer.help

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.gycss.app.databinding.ActivityVolunteerTasksBinding
import com.gycss.app.ui.chat.ChatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VolunteerTasksFragment : Fragment() {

    private var _binding: ActivityVolunteerTasksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolunteerTasksViewModel by viewModels()
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ActivityVolunteerTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupTabs()
        setupObservers()

        viewModel.fetchTasks(listOf("Accepted", "In Progress", "CompletedByVolunteer"))
    }

    private fun setupToolbar() {
        binding.btnBack.visibility = View.GONE // Hide back button in fragment
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onChatClick = { viewModel.getOrCreateChat(it) },
            onCompleteClick = { viewModel.completeTask(it.requestId) }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
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
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            if (tasks.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvTasks.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvTasks.visibility = View.VISIBLE
                adapter.submitList(tasks)
            }
        }

        viewModel.chatId.observe(viewLifecycleOwner) { chatId ->
            val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra("CHAT_ID", chatId)
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
