package com.gycss.app

import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.gycss.app.data.repository.UserRepository
import com.gycss.app.databinding.ActivityMainBinding
import com.gycss.app.ui.auth.OnboardingPermissionsActivity
import com.gycss.app.ui.auth.RoleSelectionActivity
import com.gycss.app.ui.senior.SeniorDashboardActivity
import com.gycss.app.ui.splash.NavigationEvent
import com.gycss.app.ui.splash.SplashViewModel
import com.gycss.app.ui.volunteer.VolunteerDashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: SplashViewModel by viewModels()
    private val animators = mutableListOf<ValueAnimator>()

    @Inject
    lateinit var userRepository: UserRepository
    
    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupObservers()
        startAnimations()

        updateFcmTokenIfLoggedIn()
        viewModel.decideNextScreen()
    }

    private fun updateFcmTokenIfLoggedIn() {
        val uid = auth.currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                CoroutineScope(Dispatchers.IO).launch {
                    userRepository.updateFcmToken(uid, token)
                }
            }
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    private fun setupObservers() {
        viewModel.navigationEvent.observe(this) { event ->
            when (event) {
                NavigationEvent.ToOnboarding -> navigateTo(OnboardingPermissionsActivity::class.java)
                NavigationEvent.ToRoleSelection -> navigateTo(RoleSelectionActivity::class.java)
                NavigationEvent.ToSeniorDashboard -> navigateTo(SeniorDashboardActivity::class.java)
                NavigationEvent.ToVolunteerDashboard -> navigateTo(VolunteerDashboardActivity::class.java)
                NavigationEvent.ToAdminDashboard -> {
                    Toast.makeText(this, "Admin Dashboard coming soon", Toast.LENGTH_SHORT).show()
                    navigateTo(RoleSelectionActivity::class.java)
                }
                else -> {}
            }
        }
    }

    private fun navigateTo(destination: Class<*>) {
        startActivity(Intent(this, destination))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun startAnimations() {
        animateSplashText()
        animateLogoPulse()
        animateWaves()
        animateLoadingDots()
    }

    private fun animateSplashText() {
        binding.tvTitle.apply {
            alpha = 0f
            translationY = 30f
            animate().alpha(1f).translationY(0f).setDuration(800).setStartDelay(400).start()
        }
        binding.tvSubtitle.apply {
            alpha = 0f
            translationY = 20f
            animate().alpha(1f).translationY(0f).setDuration(800).setStartDelay(700).start()
        }
    }

    private fun animateLogoPulse() {
        binding.ivLogo.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
            animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(1000).withEndAction {
                val pulseAnimator = AnimatorInflater.loadAnimator(context, R.animator.splash_logo_pulse)
                pulseAnimator.setTarget(this)
                pulseAnimator.start()
            }.start()
        }
    }

    private fun animateWaves() {
        val waveMoveFront = ValueAnimator.ofFloat(0f, -800f).apply {
            duration = 6000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addUpdateListener { 
                val value = it.animatedValue as Float
                binding.ivWaveFront.translationX = value 
            }
        }

        val bobbing = ValueAnimator.ofFloat(-15f, 15f).apply {
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Float
                binding.ivWaveFront.translationY = value
            }
        }

        animators.addAll(listOf(waveMoveFront, bobbing))
        animators.forEach { it.start() }
    }

    private fun animateLoadingDots() {
        listOf(binding.dot1, binding.dot2, binding.dot3).forEachIndexed { index, dot ->
            val dotAnim = AnimatorInflater.loadAnimator(this, R.animator.splash_dot_blink)
            dotAnim.setTarget(dot)
            dotAnim.startDelay = (index * 250).toLong()
            dotAnim.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        animators.forEach { it.cancel() }
        animators.clear()
    }
}
