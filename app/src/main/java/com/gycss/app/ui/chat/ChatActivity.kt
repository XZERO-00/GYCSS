package com.gycss.app.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.gycss.app.databinding.ActivityChatBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: MessageAdapter
    
    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val chatId = intent.getStringExtra("CHAT_ID")
        if (chatId == null) {
            finish()
            return
        }
        viewModel.chatId = chatId

        setupRecyclerView()
        setupListeners()
        setupObservers()
        
        viewModel.observeMessages()
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(auth.currentUser?.uid ?: "")
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = this@ChatActivity.adapter
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendMessage(text)
                binding.etMessage.text.clear()
            }
        }
    }

    private fun setupObservers() {
        viewModel.messages.observe(this) { messages ->
            adapter.submitList(messages) {
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }

        viewModel.sendResult.observe(this) { result ->
            result.onFailure {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
