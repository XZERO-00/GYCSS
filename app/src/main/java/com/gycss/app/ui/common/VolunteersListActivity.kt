package com.gycss.app.ui.common

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.gycss.app.R
import com.gycss.app.data.model.Volunteer
import com.gycss.app.databinding.ActivityVolunteersListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VolunteersListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteersListBinding
    private lateinit var adapter: VolunteersAdapter
    private var isLeaderboardMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteersListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isLeaderboardMode = intent.getBooleanExtra("LEADERBOARD_MODE", false)
        setupUI()
        loadVolunteers()
    }

    private fun setupUI() {
        binding.tvTitle.text = if (isLeaderboardMode) "Leaderboard" else "Our Volunteers"
        
        binding.btnBack.setOnClickListener {
            finish()
        }

        adapter = VolunteersAdapter(isLeaderboardMode) { volunteer ->
            val intent = Intent(this, VolunteerReviewActivity::class.java)
            intent.putExtra("VOLUNTEER_NAME", volunteer.name)
            intent.putExtra("VOLUNTEER_RATING", volunteer.rating)
            intent.putExtra("VOLUNTEER_HELP_COUNT", volunteer.helpCount)
            if (isLeaderboardMode) {
                intent.putExtra("IS_VOLUNTEER_VIEWING", true)
            }
            startActivity(intent)
        }

        binding.rvVolunteers.layoutManager = LinearLayoutManager(this)
        binding.rvVolunteers.adapter = adapter
    }

    private fun loadVolunteers() {
        // Mock Data with Indian Names and Professions
        val volunteers = listOf(
            Volunteer(name = "Amit Sharma", rating = 4.9f, helpCount = 45, occupation = "Software Engineer"),
            Volunteer(name = "Priya Singh", rating = 4.8f, helpCount = 38, occupation = "Doctor"),
            Volunteer(name = "Rahul Verma", rating = 4.7f, helpCount = 32, occupation = "Final Year Student"),
            Volunteer(name = "Sneha Gupta", rating = 4.6f, helpCount = 28, occupation = "School Teacher"),
            Volunteer(name = "Vikram Malhotra", rating = 4.5f, helpCount = 25, occupation = "Businessman")
        )

        val listToShow = if (isLeaderboardMode) {
            volunteers.sortedByDescending { it.helpCount }
        } else {
            volunteers
        }

        adapter.submitList(listToShow)
    }

    class VolunteersAdapter(
        private val isLeaderboard: Boolean,
        private val onClick: (Volunteer) -> Unit
    ) : RecyclerView.Adapter<VolunteersAdapter.ViewHolder>() {

        private var list = listOf<Volunteer>()

        fun submitList(newList: List<Volunteer>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_volunteer, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(list[position], position, isLeaderboard, onClick)
        }

        override fun getItemCount() = list.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val cvRoot: MaterialCardView = itemView.findViewById(R.id.cv_root)
            private val tvRank: TextView = itemView.findViewById(R.id.tv_rank)
            private val tvName: TextView = itemView.findViewById(R.id.tv_name)
            private val tvRole: TextView = itemView.findViewById(R.id.tv_role)
            private val tvRating: TextView = itemView.findViewById(R.id.tv_rating)
            private val tvHelped: TextView = itemView.findViewById(R.id.tv_helped)
            private val ivProfilePic: ShapeableImageView = itemView.findViewById(R.id.iv_profile_pic)
            private val tvLetterPlaceholder: TextView = itemView.findViewById(R.id.tv_letter_placeholder)

            fun bind(volunteer: Volunteer, position: Int, isLeaderboard: Boolean, onClick: (Volunteer) -> Unit) {
                tvName.text = volunteer.name
                tvRole.text = volunteer.occupation
                tvRating.text = "★ ${volunteer.rating}"
                tvHelped.text = "${volunteer.helpCount} Helped"

                // --- Handle Profile Picture / Letter Placeholder Logic ---
                // For demo, we always show letter placeholder if no real image URL exists
                if (volunteer.idProofUrl.isEmpty()) {
                    ivProfilePic.visibility = View.GONE
                    tvLetterPlaceholder.visibility = View.VISIBLE
                    tvLetterPlaceholder.text = volunteer.name.firstOrNull()?.uppercase() ?: "V"
                } else {
                    ivProfilePic.visibility = View.VISIBLE
                    tvLetterPlaceholder.visibility = View.GONE
                    // Use Glide to load volunteer.idProofUrl into ivProfilePic in real app
                }

                if (isLeaderboard) {
                    tvRank.visibility = View.VISIBLE
                    tvRank.text = "#${position + 1}"
                    if (position == 0) tvRank.setTextColor(itemView.context.getColor(R.color.warning_red)) 
                    else tvRank.setTextColor(itemView.context.getColor(R.color.primary_teal))
                } else {
                    tvRank.visibility = View.GONE
                }

                cvRoot.setOnClickListener { onClick(volunteer) }
            }
        }
    }
}