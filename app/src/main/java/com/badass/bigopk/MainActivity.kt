package com.badass.bigopk

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private const val WEBSITE_URL = "https://badassopkx.com/"

class MainActivity : ComponentActivity() {
    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFFD40000))) {
                BadassWebApp(onWebViewCreated = { webView = it })
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    private fun BadassWebApp(onWebViewCreated: (WebView) -> Unit) {
        var loading by remember { mutableStateOf(true) }
        var failed by remember { mutableStateOf(false) }
        var currentWebView by remember { mutableStateOf<WebView?>(null) }

        BackHandler(enabled = currentWebView?.canGoBack() == true) {
            currentWebView?.goBack()
        }

        Box(Modifier.fillMaxSize().background(Color(0xFF090909))) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.loadsImagesAutomatically = true
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        settings.setSupportMultipleWindows(false)
                        settings.allowFileAccess = false
                        settings.allowContentAccess = true
                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                loading = true
                                failed = false
                            }
                            override fun onPageFinished(view: WebView?, url: String?) {
                                loading = false
                                failed = false
                            }
                            override fun onReceivedError(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    loading = false
                                    failed = true
                                }
                            }
                        }
                        currentWebView = this
                        onWebViewCreated(this)
                        loadUrl(WEBSITE_URL)
                    }
                },
                update = { currentWebView = it }
            )

            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFD40000)
                )
            }

            if (failed) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Unable to load BADASS website.", color = Color.White)
                    Button(
                        onClick = { failed = false; loading = true; currentWebView?.loadUrl(WEBSITE_URL) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD40000))
                    ) { Text("TRY AGAIN") }
                }
            }
        }
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (webView?.canGoBack() == true) webView?.goBack() else super.onBackPressed()
    }
}
