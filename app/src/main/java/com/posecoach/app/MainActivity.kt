package com.posecoach.app

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    private val TARGET_URL = "https://angle-assist-pro.lovable.app/"
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001

    /** True once the very first page has fully loaded (skip fade for reloads) */
    private var hasLoadedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Edge-to-edge & Immersive Mode ─────────────────────────────
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        // Hide both status and navigation bars for a true full-screen experience
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        // Make them reappear only briefly when the user swipes from the edge
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Ensure icons are appropriate (though they will be hidden most of the time)
        insetsController.isAppearanceLightStatusBars     = false
        insetsController.isAppearanceLightNavigationBars = false

        setContentView(R.layout.activity_main)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        webView            = findViewById(R.id.webView)
        progressBar        = findViewById(R.id.progressBar)

        setupWebView()
        setupSwipeRefresh()
        setupBackNavigation()
        setupResponsiveInsets()
        setupDownloadListener()

        // ── Runtime camera permission ─────────────────────────────────
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            loadWebView()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // WebView setup
    // ─────────────────────────────────────────────────────────────────

    private fun setupWebView() {
        with(webView.settings) {
            javaScriptEnabled                = true
            domStorageEnabled                = true
            databaseEnabled                  = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess                  = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(false)
            useWideViewPort                  = true
            loadWithOverviewMode             = true
            builtInZoomControls              = false
            displayZoomControls              = false
            setSupportZoom(true)

            // High-performance rendering
            cacheMode = WebSettings.LOAD_DEFAULT
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true

            // Ensure the web app's font rendering is crisp
            textZoom = 100
        }

        // Disable standard overscroll to keep the premium feel
        webView.overScrollMode = View.OVER_SCROLL_NEVER

        // Add JavaScript Interface for image saving
        webView.addJavascriptInterface(ImageDownloadInterface(this), "AndroidDownload")

        // Performance: Enable hardware acceleration
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // Keep a pure black background while the page is loading
        webView.setBackgroundColor(Color.parseColor("#080808"))

        // ── WebViewClient ─────────────────────────────────────────────
        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: android.graphics.Bitmap?
            ) {
                super.onPageStarted(view, url, favicon)
                showProgress()
                swipeRefreshLayout.isRefreshing = false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                hideProgress()
                swipeRefreshLayout.isRefreshing = false

                // Fade the WebView in smoothly on the very first load
                if (!hasLoadedOnce) {
                    hasLoadedOnce = true
                    fadeInWebView()
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                hideProgress()
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // ── WebChromeClient ───────────────────────────────────────────
        webView.webChromeClient = object : WebChromeClient() {

            override fun onPermissionRequest(request: PermissionRequest?) {
                // Grant camera and microphone so getUserMedia() works for
                // the live posing / pose-capture feature
                request?.grant(
                    arrayOf(
                        PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                        PermissionRequest.RESOURCE_AUDIO_CAPTURE
                    )
                )
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                if (newProgress >= 100) hideProgress() else showProgress()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Progress bar helpers
    // ─────────────────────────────────────────────────────────────────

    private fun showProgress() {
        if (progressBar.visibility != View.VISIBLE) {
            progressBar.alpha = 0f
            progressBar.visibility = View.VISIBLE
            progressBar.animate()
                .alpha(1f)
                .setDuration(200)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun hideProgress() {
        if (progressBar.visibility == View.VISIBLE) {
            progressBar.animate()
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction { progressBar.visibility = View.GONE }
                .start()
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // WebView fade-in on first load
    // ─────────────────────────────────────────────────────────────────

    private fun fadeInWebView() {
        webView.alpha = 0f
        webView.visibility = View.VISIBLE
        webView.animate()
            .alpha(1f)
            .setDuration(500)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    // ─────────────────────────────────────────────────────────────────
    // Pull-to-refresh
    // ─────────────────────────────────────────────────────────────────

    private fun setupSwipeRefresh() {
        // Gold spinner to match the luxury theme
        swipeRefreshLayout.setColorSchemeColors(
            Color.parseColor("#C9A84C"),  // gold
            Color.parseColor("#E8C96A"),  // gold light
            Color.parseColor("#8B6914")   // gold dark
        )
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(
            Color.parseColor("#1A1A1A")   // obsidian surface
        )

        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
            // Tactile feedback on pull-to-refresh
            swipeRefreshLayout.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Back navigation
    // ─────────────────────────────────────────────────────────────────

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }

    /**
     * Ensures the WebView handles window insets (notches, system bars) correctly.
     * Overriding the default behavior prevents double-padding and allows web content
     * to use CSS env(safe-area-inset-*) variables for a true luxury experience.
     */
    private fun setupResponsiveInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(webView) { _, windowInsets ->
            // By returning the original windowInsets, we allow WebView to handle
            // safe areas (cutouts, bars) via CSS safe-area-insets.
            windowInsets
        }
    }

    /**
     * Listens for download requests (like clicking a "Save Photo" button).
     * Handles blob: URLs by converting them to Base64 in the WebView context.
     */
    private fun setupDownloadListener() {
        webView.setDownloadListener { url, _, _, _, _ ->
            if (url.startsWith("blob:")) {
                val js = """
                    var xhr = new XMLHttpRequest();
                    xhr.open('GET', '$url', true);
                    xhr.responseType = 'blob';
                    xhr.onload = function(e) {
                        if (this.status == 200) {
                            var blob = this.response;
                            var reader = new FileReader();
                            reader.readAsDataURL(blob);
                            reader.onloadend = function() {
                                var base64data = reader.result;
                                AndroidDownload.processBase64Image(base64data);
                            }
                        }
                    };
                    xhr.send();
                """.trimIndent()
                webView.evaluateJavascript(js, null)
            } else if (url.startsWith("data:image")) {
                // Direct data URL
                val interfaceObj = ImageDownloadInterface(this)
                interfaceObj.processBase64Image(url)
            }
        }
    }

    /**
     * JavaScript Interface to receive Base64 image data from the WebView.
     */
    class ImageDownloadInterface(private val context: Context) {
        @JavascriptInterface
        fun processBase64Image(base64Data: String) {
            try {
                // Remove the header (e.g., "data:image/png;base64,")
                val pureBase64 = base64Data.substringAfter(",")
                val imageBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                saveImageToGallery(imageBytes)
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Failed to process image", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun saveImageToGallery(bytes: ByteArray) {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return
            val filename = "PoseCoach_${System.currentTimeMillis()}.png"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PoseCoach")
            }

            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                context.contentResolver.openOutputStream(it).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream!!)
                }
                Handler(Looper.getMainLooper()).post {
                    // Tactile confirmation
                    (context as? MainActivity)?.window?.decorView?.performHapticFeedback(
                        HapticFeedbackConstants.CONFIRM
                    )
                    Toast.makeText(context, "Photo saved to Gallery", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────

    private fun loadWebView() {
        webView.loadUrl(TARGET_URL)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            // Load regardless of grant/deny; site handles fallback gracefully
            loadWebView()
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
