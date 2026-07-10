package com.posecoach.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Luxury splash screen shown on every cold start.
 *
 * Sequence:
 *   1. Logo scales + fades in (900 ms)
 *   2. Tagline slides up + fades in (700 ms, offset 600 ms)
 *   3. Three gold dots pulse in a wave (infinite, until transition)
 *   4. After 2 200 ms total, screen fades out → MainActivity enters
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge: hide both status and navigation bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContentView(R.layout.activity_splash)

        val logoContainer = findViewById<View>(R.id.logoContainer)
        val appName      = findViewById<View>(R.id.appName)
        val tagline      = findViewById<View>(R.id.tagline)
        val dot1         = findViewById<View>(R.id.dot1)
        val dot2         = findViewById<View>(R.id.dot2)
        val dot3         = findViewById<View>(R.id.dot3)

        // ── Logo entrance ──────────────────────────────────────────
        logoContainer.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.logo_enter)
        )

        // ── Text entrance ──────────────────────────────────────────
        val textEnter = AnimationUtils.loadAnimation(this, R.anim.text_enter)
        appName.startAnimation(textEnter)
        tagline.startAnimation(textEnter)

        // ── Pulsing dot wave ───────────────────────────────────────
        pulseDot(dot1, delayMs = 0L)
        pulseDot(dot2, delayMs = 180L)
        pulseDot(dot3, delayMs = 360L)

        // ── Navigate after 2 400 ms ────────────────────────────────
        window.decorView.postDelayed({
            navigateToMain()
        }, 2_400L)
    }

    /**
     * Applies a repeating scale + alpha pulse to a single dot view.
     *
     * @param dot     the View to animate
     * @param delayMs stagger offset so the dots pulse in a wave
     */
    private fun pulseDot(dot: View, delayMs: Long) {
        val pulse = android.view.animation.AnimationSet(true).apply {
            interpolator = DecelerateInterpolator()
            repeatCount  = android.view.animation.Animation.INFINITE

            addAnimation(android.view.animation.ScaleAnimation(
                0.4f, 1.0f, 0.4f, 1.0f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration    = 600
                repeatMode  = android.view.animation.Animation.REVERSE
                repeatCount = android.view.animation.Animation.INFINITE
            })

            addAnimation(android.view.animation.AlphaAnimation(0.25f, 1.0f).apply {
                duration    = 600
                repeatMode  = android.view.animation.Animation.REVERSE
                repeatCount = android.view.animation.Animation.INFINITE
            })

            startOffset = delayMs
        }
        dot.startAnimation(pulse)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        // Custom transition: splash fades out, main fades in
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}
