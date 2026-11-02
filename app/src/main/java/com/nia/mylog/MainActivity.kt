package com.nia.mylog

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebViewAssetLoader
import android.webkit.WebViewClient
import org.json.JSONObject

/**
  * 마이로그: 앱 안에 들어 있는 웹 화면(assets/index.html)을 보여주는 화면 하나짜리 앱이에요.
 * 기록은 이 휴대폰의 앱 저장소(localStorage)에만 남고, 백업 파일은 사용자가 고른 곳에 저장해요.
 */
class MainActivity : ComponentActivity() {

    private lateinit var web: WebView
    private var pendingBackup: String? = null
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    // 백업 저장: 안드로이드의 "다른 이름으로 저장" 화면을 열어요.
    private val createBackup =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            val data = pendingBackup
            pendingBackup = null
            if (uri == null || data == null) return@registerForActivityResult
            val ok = runCatching {
                contentResolver.openOutputStream(uri)?.use { it.write(data.toByteArray(Charsets.UTF_8)) }
                    ?: error("no stream")
            }.isSuccess
            toastInPage(if (ok) "백업 파일을 저장했어요" else "백업 파일을 저장하지 못했어요")
        }

    // 백업 불러오기: 웹 화면의 파일 선택 버튼이 이 화면을 열어요.
    private val openBackup =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            fileCallback?.onReceiveValue(if (uri != null) arrayOf(uri) else null)
            fileCallback = null
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val paper = if (dark) Color.BLACK else Color.WHITE
        window.decorView.setBackgroundColor(paper)

        web = WebView(this)
        web.setBackgroundColor(paper)
        setContentView(web)

        // 상태 표시줄, 내비게이션 바, 키보드에 화면이 가려지지 않게 여백을 줘요.
        ViewCompat.setOnApplyWindowInsetsListener(web) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout() or
                    WindowInsetsCompat.Type.ime()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        // 앱 안의 파일을 https 주소처럼 읽어서, 저장소(localStorage)가 안정적으로 유지되게 해요.
        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            textZoom = 100
        }
        web.addJavascriptInterface(Bridge(), "AndroidBridge")

        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                loader.shouldInterceptRequest(request.url)

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (request.url.host == APP_HOST) return false
                // 바깥 링크는 브라우저로 열어요.
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, request.url))
                } catch (e: ActivityNotFoundException) {
                }
                return true
            }
        }

        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                callback: ValueCallback<Array<Uri>>,
                params: FileChooserParams
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = callback
                return try {
                    openBackup.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
                    true
                } catch (e: ActivityNotFoundException) {
                    fileCallback = null
                    false
                }
            }
        }

        // 뒤로 가기: 열린 창 닫기 → 첫 페이지로 → 앱 닫기 순서예요.
        onBackPressedDispatcher.addCallback(this) {
            web.evaluateJavascript("window.bujoBack ? String(window.bujoBack()) : 'false'") { result ->
                if (result != "\"true\"") {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        }

        if (savedInstanceState == null || web.restoreState(savedInstanceState) == null) {
            web.loadUrl("https://$APP_HOST/assets/index.html")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    override fun onDestroy() {
        fileCallback?.onReceiveValue(null)
        fileCallback = null
        web.destroy()
        super.onDestroy()
    }

    private fun toastInPage(message: String) {
        web.evaluateJavascript("window.bujoToast && window.bujoToast(${JSONObject.quote(message)})", null)
    }

    /** 웹 화면에서 부르는 함수들 */
    inner class Bridge {
        @JavascriptInterface
        fun saveBackup(fileName: String, json: String) {
            runOnUiThread {
                pendingBackup = json
                try {
                    createBackup.launch(fileName)
                } catch (e: ActivityNotFoundException) {
                    pendingBackup = null
                    toastInPage("이 기기에서는 파일을 저장할 수 없어요")
                }
            }
        }
    }

    private companion object {
        const val APP_HOST = "appassets.androidplatform.net"
    }
}
