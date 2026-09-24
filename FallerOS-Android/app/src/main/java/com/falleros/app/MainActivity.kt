package com.falleros.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import java.io.File
import java.io.FileOutputStream

/**
 * MainActivity — natywna powłoka Faller'OS.
 *
 * Cały system (index.html, manifest.json, service-worker.js, ikony) jest
 * spakowany w app/src/main/assets/www i serwowany lokalnie przez
 * WebViewAssetLoader pod domeną https://appassets.androidplatform.net/ —
 * dzięki temu działają Service Worker, localStorage, fetch() itd. tak samo
 * jak w przeglądarce, ale bez żadnego prawdziwego serwera i bez internetu
 * (poza funkcjami, które go faktycznie wymagają: Python/Pyodide, pip,
 * pakowanie ZIP przy pierwszym użyciu i tłumaczenie tekstu).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null

    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val callback = fileChooserCallback
            fileChooserCallback = null
            if (callback == null) return@registerForActivityResult
            if (result.resultCode != Activity.RESULT_OK || result.data == null) {
                callback.onReceiveValue(null)
                return@registerForActivityResult
            }
            val data = result.data!!
            val uris: Array<Uri> = when {
                data.clipData != null -> {
                    val clip = data.clipData!!
                    Array(clip.itemCount) { i -> clip.getItemAt(i).uri }
                }
                data.data != null -> arrayOf(data.data!!)
                else -> emptyArray()
            }
            callback.onReceiveValue(uris)
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        webView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        setContentView(webView)

        // Zostaw miejsce na pasek stanu / gesty systemowe (podobnie jak safe-area-inset w wersji web).
        ViewCompat.setOnApplyWindowInsetsListener(webView) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", AssetsPathHandler(this))
            .build()

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true          // wymagane dla localStorage systemu plików Faller'OS
        settings.databaseEnabled = true
        settings.allowFileAccess = false            // dostęp do plików idzie przez WebViewAssetLoader, nie file://
        settings.mediaPlaybackRequiresUserGesture = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.setSupportZoom(false)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView, request: WebResourceRequest
            ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean {
                val uri = request.url
                // Zasoby własnej domeny (index.html, manifest, ikony) zostają w WebView.
                if (uri.host == "appassets.androidplatform.net") return false
                // Wszystko inne (np. link kliknięty w treści) otwórz w normalnej przeglądarce.
                return try {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView,
                callback: ValueCallback<Array<Uri>>,
                params: FileChooserParams
            ): Boolean {
                fileChooserCallback?.onReceiveValue(null)
                fileChooserCallback = callback
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.mode == FileChooserParams.MODE_OPEN_MULTIPLE)
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                return try {
                    fileChooserLauncher.launch(Intent.createChooser(intent, "Wybierz plik"))
                    true
                } catch (e: Exception) {
                    fileChooserCallback = null
                    false
                }
            }
        }

        // Obsługa "Pobierz na dysk" z Menedżera plików Faller'OS (linki data: URI).
        webView.setDownloadListener { url, _, contentDisposition, mimeType, _ ->
            if (url.startsWith("data:")) {
                saveDataUri(url, contentDisposition, mimeType)
            } else {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (e: Exception) {
                    Toast.makeText(this, "Nie udało się otworzyć pliku.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            if (webView.canGoBack()) webView.goBack() else finish()
        }

        webView.loadUrl("https://appassets.androidplatform.net/assets/www/index.html")
    }

    /** Dekoduje link "data:...;base64,...." (np. pobrany plik z Menedżera plików) i zapisuje go na urządzeniu. */
    private fun saveDataUri(dataUri: String, contentDisposition: String?, mimeType: String?) {
        try {
            val commaIdx = dataUri.indexOf(',')
            if (commaIdx < 0) return
            val meta = dataUri.substring(5, commaIdx)   // np. "text/plain;charset=utf-8" albo "image/png;base64"
            val payload = dataUri.substring(commaIdx + 1)
            val isBase64 = meta.contains("base64")

            val bytes: ByteArray = if (isBase64) {
                Base64.decode(payload, Base64.DEFAULT)
            } else {
                Uri.decode(payload).toByteArray(Charsets.UTF_8)
            }

            val fileName = extractFileName(contentDisposition) ?: ("falleros_" + System.currentTimeMillis())
            val dir = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "")
            dir.mkdirs()
            val outFile = File(dir, fileName)
            FileOutputStream(outFile).use { it.write(bytes) }

            Toast.makeText(
                this,
                "Zapisano: " + outFile.absolutePath,
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Nie udało się zapisać pliku.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractFileName(contentDisposition: String?): String? {
        if (contentDisposition == null) return null
        val marker = "filename="
        val idx = contentDisposition.indexOf(marker)
        if (idx < 0) return null
        return contentDisposition.substring(idx + marker.length).trim('"', ' ')
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
