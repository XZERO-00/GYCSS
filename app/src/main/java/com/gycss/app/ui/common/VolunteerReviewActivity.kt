package com.gycss.app.ui.common

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.gycss.app.R
import com.gycss.app.data.model.Review
import com.gycss.app.data.repository.FirestoreRepository
import com.gycss.app.databinding.ActivityVolunteerReviewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VolunteerReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVolunteerReviewBinding
    private lateinit var adapter: ReviewsAdapter
    private var isVolunteerViewing = false
    private var reviewsListener: ListenerRegistration? = null
    private var volunteerId: String = ""

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunteerReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        volunteerId = intent.getStringExtra("VOLUNTEER_ID") ?: ""
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
        
        loadReviews()
    }

    private fun setupHeader(name: String, rating: Float, helpCount: Int) {
        binding.tvVolunteerName.text = name
        binding.tvRatingLarge.text = "%.1f".format(rating)
        binding.tvHelpedCount.text = "$helpCount People Helped"
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupReviewsList() {
        adapter = ReviewsAdapter(emptyList())
        binding.rvReviews.layoutManager = LinearLayoutManager(this)
        binding.rvReviews.adapter = adapter
    }

    private fun loadReviews() {
        if (volunteerId.isEmpty()) return
        
        reviewsListener = FirestoreRepository.getVolunteerReviews(volunteerId) { reviews ->
            runOnUiThread {
                adapter.updateList(reviews)
                if (reviews.isEmpty()) {
                    binding.tvEmptyReviews.visibility = View.VISIBLE
                } else {
                    binding.tvEmptyReviews.visibility = View.GONE
                }
            }
        }
    }

    private fun setupSubmitReview(volunteerName: String) {
        binding.btnSubmitReview.setOnClickListener {
            val rating = binding.ratingBar.rating
            val comment = binding.etReviewComment.text.toString().trim()

            if (rating == 0f) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser
            val review = Review(
                volunteerId = volunteerId,
                seniorId = user?.uid ?: "unknown",
                seniorName = user?.displayName ?: "Senior Member",
                rating = rating,
                comment = comment
            )

            binding.btnSubmitReview.isEnabled = false
            FirestoreRepository.submitReview(review, onSuccess = {
                runOnUiThread {
                    binding.btnSubmitReview.isEnabled = true
                    Toast.makeText(this, "Review Submitted!", Toast.LENGTH_SHORT).show()
                    binding.etReviewComment.text?.clear()
                    binding.ratingBar.rating = 0f
                }
            }, onFailure = {
                runOnUiThread {
                    binding.btnSubmitReview.isEnabled = true
                    Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        reviewsListener?.remove()
    }

    class ReviewsAdapter(private var list: List<Review>) : androidx.recyclerview.widget.RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {
        
        fun updateList(newList: List<Review>) {
            list = newList
            notifyDataSetChanged()
        }

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
            private val tvLetterPlaceholder: TextView = itemView.findViewById(com.gycss.app.R.id.tv_letter_placeholder)

            fun bind(review: Review) {
                tvName.text = review.seniorName
                tvComment.text = review.comment
                rbRating.rating = review.rating
                tvLetterPlaceholder.text = review.seniorName.firstOrNull()?.uppercase() ?: "S"
            }
        }
    }
}
