package com.gycss.app.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gycss.app.data.model.Message
import com.gycss.app.data.repository.AuthRepository
import com.gycss.app.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _sendResult = MutableLiveData<Result<Unit>>()
    val sendResult: LiveData<Result<Unit>> = _sendResult
    
    lateinit var chatId: String

    fun observeMessages() {
        viewModelScope.launch {
            chatRepository.observeMessages(chatId).collectLatest {
                _messages.postValue(it)
            }
        }
    }

    fun sendMessage(messageText: String) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            val message = Message(
                senderId = user.uid,
                senderName = user.name,
                messageText = messageText
            )
            val result = chatRepository.sendMessage(chatId, message)
            _sendResult.postValue(result)
        }
    }
}
