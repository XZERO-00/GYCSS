package com.gycss.app.ui.senior

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gycss.app.data.model.HelpRequest
import com.gycss.app.databinding.ItemActiveRequestBinding

class ActiveRequestAdapter(
    private val onCancelClick: (String) -> Unit,
    private val onConfirmClick: (String) -> Unit,
    private val onRejectClick: (String) -> Unit
) : ListAdapter<HelpRequest, ActiveRequestAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActiveRequestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = getItem(position)
        holder.bind(request, onCancelClick, onConfirmClick, onRejectClick)
    }

    class ViewHolder(private val binding: ItemActiveRequestBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            request: HelpRequest, 
            onCancelClick: (String) -> Unit,
            onConfirmClick: (String) -> Unit,
            onRejectClick: (String) -> Unit
        ) {
            binding.tvTitle.text = request.title
            binding.tvCategoryTag.text = request.category.uppercase()
            binding.tvStatus.text = request.status.uppercase()
            
            if (request.volunteerName != null) {
                binding.tvVolunteerInfo.visibility = View.VISIBLE
                binding.tvVolunteerInfo.text = "Assigned: ${request.volunteerName}"
            } else {
                binding.tvVolunteerInfo.visibility = View.GONE
            }

            // Handle Completion Confirmation state
            if (request.status == "CompletedByVolunteer") {
                binding.tvCompletionMsg.visibility = View.VISIBLE
                binding.layoutConfirmation.visibility = View.VISIBLE
                binding.tvStatus.text = "AWAITING CONFIRMATION"
            } else {
                binding.tvCompletionMsg.visibility = View.GONE
                binding.layoutConfirmation.visibility = View.GONE
            }

            binding.btnConfirm.setOnClickListener { onConfirmClick(request.requestId) }
            binding.btnReject.setOnClickListener { onRejectClick(request.requestId) }

            binding.root.setOnLongClickListener {
                if (request.status == "Pending") {
                    onCancelClick(request.requestId)
                }
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HelpRequest>() {
        override fun areItemsTheSame(oldItem: HelpRequest, newItem: HelpRequest) = oldItem.requestId == newItem.requestId
        override fun areContentsTheSame(oldItem: HelpRequest, newItem: HelpRequest) = oldItem == newItem
    }
}
