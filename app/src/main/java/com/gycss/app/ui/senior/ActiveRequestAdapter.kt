package com.gycss.app.ui.senior

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gycss.app.data.model.HelpRequest

class ActiveRequestAdapter(private val onCancelClick: (String) -> Unit) :
    ListAdapter<HelpRequest, ActiveRequestAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = getItem(position)
        holder.bind(request, onCancelClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(request: HelpRequest, onCancelClick: (String) -> Unit) {
            text1.text = "${request.category}: ${request.title}"
            text2.text = "Status: ${request.status} - ${request.description}"
            
            itemView.setOnLongClickListener {
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
