//CODE ATTRIBUTION
//01
//Android Activity
//Adapted from: Android Developers. (2025). Activity. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/app/Activity
//Date Accessed: 21 October 2025

//02
//WebView
//Adapted from: Android Developers. (2025). WebView. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/webkit/WebView
//Date Accessed: 21 October 2025

//03
//Material Design Components
//Adapted from: Material Design. (2025). Material Design Components. [online]
//Available at: https://material.io/components
//Date Accessed: 21 October 2025

package vcmsa.projects.careerconnect.ui.recruiter

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import vcmsa.projects.careerconnect.R

/**
 * Activity for viewing CV files in a WebView
 */
class CVViewerActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: MaterialTextView
    private lateinit var btnBack: MaterialButton

    private var cvUrl: String? = null
    private var fileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cv_viewer)

        // Get CV URL and filename from intent
        cvUrl = intent.getStringExtra("cv_url")
        fileName = intent.getStringExtra("file_name")
        
        if (cvUrl.isNullOrEmpty()) {
            Toast.makeText(this, "CV URL not provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupWebView()
        loadCV()
    }

    private fun initializeViews() {
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        tvTitle = findViewById(R.id.tvTitle)
        btnBack = findViewById(R.id.btnBack)

        // Set title
        tvTitle.text = fileName ?: "CV Viewer"

        // Back button
        btnBack.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // Handle PDF viewing
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                progressBar.visibility = View.GONE
                Toast.makeText(this@CVViewerActivity, "Error loading CV: $description", Toast.LENGTH_LONG).show()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
            }
        }
    }

    private fun loadCV() {
        val url = cvUrl ?: return
        
        progressBar.visibility = View.VISIBLE
        
        // For PDF files, try to load with Google Docs viewer or PDF.js
        val pdfViewerUrl = if (url.endsWith(".pdf", ignoreCase = true)) {
            "https://docs.google.com/gview?embedded=true&url=$url"
        } else {
            url
        }
        
        webView.loadUrl(pdfViewerUrl)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
