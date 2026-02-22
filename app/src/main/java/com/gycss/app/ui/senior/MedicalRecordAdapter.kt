package com.gycss.app.ui.senior

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gycss.app.R
import com.gycss.app.data.model.MedicalRecord
import java.text.SimpleDateFormat
import java.util.*

class MedicalRecordAdapter(private val onDeleteClick: (String) -> Unit) :
    ListAdapter<MedicalRecord, MedicalRecordAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = getItem(position)
        holder.bind(record, onDeleteClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(record: MedicalRecord, onDeleteClick: (String) -> Unit) {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            text1.text = record.description
            text2.text = "Dr. ${record.doctorName} - ${sdf.format(Date(record.date))}"
            
            itemView.setOnLongClickListener {
                onDeleteClick(record.id)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MedicalRecord>() {
        override fun areItemsTheSame(oldItem: MedicalRecord, newItem: MedicalRecord) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MedicalRecord, newItem: MedicalRecord) = oldItem == newItem
    }
}
