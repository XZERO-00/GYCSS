package com.gycss.app.ui.volunteer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.gycss.app.R
import com.gycss.app.data.model.Event
import com.gycss.app.data.model.EventApplication
import com.gycss.app.data.repository.FirestoreRepository
import com.gycss.app.databinding.ActivityVolunteersListBinding // Reusing layout for simplicity
import com.gycss.app.databinding.ItemEventBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VolunteerEventsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteersListBinding
    private var eventsListener: ListenerRegistration? = null
    private lateinit var adapter: EventsAdapter

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteersListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadEvents()
    }

    private fun setupUI() {
        binding.toolbar.title = "Upcoming Events"
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = EventsAdapter { event -> applyToEvent(event) }
        binding.rvVolunteers.layoutManager = LinearLayoutManager(this)
        binding.rvVolunteers.adapter = adapter
    }

    private fun loadEvents() {
        binding.progressBar.visibility = View.VISIBLE
        eventsListener = FirestoreRepository.getEvents { events ->
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                adapter.submitList(events)
                binding.layoutEmptyState.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
                binding.tvEmptyState.text = "No upcoming events found."
            }
        }
    }

    private fun applyToEvent(event: Event) {
        val user = auth.currentUser ?: return
        val application = EventApplication(
            eventId = event.id,
            volunteerId = user.uid,
            volunteerName = user.displayName ?: "Volunteer"
        )

        FirestoreRepository.applyToEvent(application, onSuccess = {
            Toast.makeText(this, "Application submitted!", Toast.LENGTH_SHORT).show()
        }, onFailure = {
            Toast.makeText(this, it.message ?: "Failed to apply", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        eventsListener?.remove()
    }

    class EventsAdapter(private val onApply: (Event) -> Unit) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {
        private var list = listOf<Event>()

        fun submitList(newList: List<Event>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount() = list.size

        inner class ViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(event: Event) {
                binding.tvEventTitle.text = event.title
                binding.tvEventCategory.text = event.category
                binding.tvEventDesc.text = event.description
                binding.tvEventLocation.text = event.location
                binding.btnApply.setOnClickListener { onApply(event) }
            }
        }
    }
}
