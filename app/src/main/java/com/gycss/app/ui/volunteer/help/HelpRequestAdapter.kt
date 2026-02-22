package com.gycss.app.ui.volunteer.help

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gycss.app.data.model.HelpRequest
import com.gycss.app.databinding.ItemHelpRequestBinding

class HelpRequestAdapter(
    private val onAcceptClick: (String) -> Unit
) : ListAdapter<HelpRequest, HelpRequestAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(val binding: ItemHelpRequestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHelpRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = getItem(position)
        holder.binding.apply {
            tvTitle.text = request.title
            tvSeniorName.text = "From: ${request.seniorName}"
            tvDescription.text = request.description
            btnAccept.setOnClickListener { onAcceptClick(request.requestId) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<HelpRequest>() {
        override fun areItemsTheSame(oldItem: HelpRequest, newItem: HelpRequest): Boolean {
            return oldItem.requestId == newItem.requestId
        }

        override fun areContentsTheSame(oldItem: HelpRequest, newItem: HelpRequest): Boolean {
            return oldItem == newItem
        }
    }
}
