package com.gycss.app.ui.volunteer.help

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gycss.app.data.model.HelpRequest
import com.gycss.app.databinding.ItemActiveTaskBinding

class TaskAdapter(
    private val onChatClick: (HelpRequest) -> Unit,
    private val onCompleteClick: (HelpRequest) -> Unit
) : ListAdapter<HelpRequest, TaskAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemActiveTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActiveTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = getItem(position)
        holder.binding.apply {
            tvTitle.text = request.title
            tvSeniorName.text = "Senior: ${request.seniorName}"
            tvDescription.text = request.description
            
            when (request.status) {
                "Completed" -> {
                    btnChat.visibility = View.GONE
                    btnComplete.visibility = View.GONE
                    tvSeniorName.text = "Senior: ${request.seniorName} • COMPLETED"
                }
                "CompletedByVolunteer" -> {
                    btnChat.visibility = View.VISIBLE
                    btnComplete.visibility = View.GONE
                    tvSeniorName.text = "Senior: ${request.seniorName} • AWAITING CONFIRMATION"
                }
                else -> {
                    btnChat.visibility = View.VISIBLE
                    btnComplete.visibility = View.VISIBLE
                }
            }

            btnChat.setOnClickListener { onChatClick(request) }
            btnComplete.setOnClickListener { onCompleteClick(request) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<HelpRequest>() {
        override fun areItemsTheSame(oldItem: HelpRequest, newItem: HelpRequest) = oldItem.requestId == newItem.requestId
        override fun areContentsTheSame(oldItem: HelpRequest, newItem: HelpRequest) = oldItem == newItem
    }
}
