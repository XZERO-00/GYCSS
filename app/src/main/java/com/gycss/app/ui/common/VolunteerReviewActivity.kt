package com.gycss.app.ui.common

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.imageview.ShapeableImageView
import com.gycss.app.R
import com.gycss.app.data.model.Review
import com.gycss.app.databinding.ActivityVolunteerReviewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VolunteerReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteerReviewBinding
    private lateinit var adapter: ReviewsAdapter
    private var isVolunteerViewing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("VOLUNTEER_NAME") ?: "Volunteer"
        val rating = intent.getFloatExtra("VOLUNTEER_RATING", 0.0f)
        val helpCount = intent.getIntExtra("VOLUNTEER_HELP_COUNT", 0)
        
        isVolunteerViewing = intent.getBooleanExtra("IS_VOLUNTEER_VIEWING", false)

        setupHeader(name, rating, helpCount)
        setupReviewsList()
        setupSubmitReview(name)
        
        if (isVolunteerViewing) {
            binding.cvWriteReview.visibility = View.GONE
        }
    }

    private fun setupHeader(name: String, rating: Float, helpCount: Int) {
        binding.tvVolunteerName.text = name
        binding.tvRatingLarge.text = rating.toString()
        binding.tvHelpedCount.text = "$helpCount People Helped"
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupReviewsList() {
        // Mock Reviews with different senior names for demo
        val reviews = listOf(
            Review(seniorName = "Rajesh Kumar", rating = 5.0f, comment = "Very helpful and polite! Arrived in 5 mins."),
            Review(seniorName = "Sunita Devi", rating = 4.0f, comment = "Arrived on time. Good service and very respectful."),
            Review(seniorName = "Anil Kapoor", rating = 5.0f, comment = "Life saver during emergency. Highly recommend!")
        )

        adapter = ReviewsAdapter(reviews)
        binding.rvReviews.layoutManager = LinearLayoutManager(this)
        binding.rvReviews.adapter = adapter
    }

    private fun setupSubmitReview(volunteerName: String) {
        binding.btnSubmitReview.setOnClickListener {
            val rating = binding.ratingBar.rating
            val comment = binding.etReviewComment.text.toString()

            if (rating == 0f) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Review Submitted for $volunteerName!", Toast.LENGTH_SHORT).show()
            binding.etReviewComment.text?.clear()
            binding.ratingBar.rating = 0f
        }
    }

    class ReviewsAdapter(private val list: List<Review>) : androidx.recyclerview.widget.RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context).inflate(com.gycss.app.R.layout.item_review, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(list[position])
        }
        override fun getItemCount() = list.size
        class ViewHolder(itemView: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            private val tvName: TextView = itemView.findViewById(com.gycss.app.R.id.tv_reviewer_name)
            private val tvComment: TextView = itemView.findViewById(com.gycss.app.R.id.tv_comment)
            private val rbRating: android.widget.RatingBar = itemView.findViewById(com.gycss.app.R.id.rb_item_rating)
            private val ivSeniorDp: ShapeableImageView = itemView.findViewById(com.gycss.app.R.id.iv_senior_dp)
            private val tvLetterPlaceholder: TextView = itemView.findViewById(com.gycss.app.R.id.tv_letter_placeholder)

            fun bind(review: Review) {
                tvName.text = review.seniorName
                tvComment.text = review.comment
                rbRating.rating = review.rating

                // Always show letter placeholder for demo since we don't have senior photos yet
                ivSeniorDp.visibility = View.GONE
                tvLetterPlaceholder.visibility = View.VISIBLE
                tvLetterPlaceholder.text = review.seniorName.firstOrNull()?.uppercase() ?: "S"
            }
        }
    }
}