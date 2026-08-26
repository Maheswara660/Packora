package com.maheswara660.packora.template

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.autofill.AutofillManager
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.maheswara660.packora.template.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private var config: JSONObject? = null
    private lateinit var insetsController: WindowInsetsControllerCompat
    private var isNightMode: Boolean = false

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (filePathCallback != null) {
            val intentData = result.data
            val results: Array<Uri>? = if (result.resultCode == Activity.RESULT_OK) {
                if (intentData?.data != null) {
                    arrayOf(intentData.data!!)
                } else if (intentData?.clipData != null) {
                    val clipData = intentData.clipData!!
                    Array(clipData.itemCount) { i -> clipData.getItemAt(i).uri }
                } else null
            } else null

            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        }
    }

    // JavaScript Bridge to trigger native Android Password Manager / Autofill Framework
    inner class AutofillBridge {
        @JavascriptInterface
        fun triggerAutofill() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                runOnUiThread {
                    val afm = getSystemService(AutofillManager::class.java)
                    if (afm != null && afm.isEnabled) {
                        afm.requestAutofill(binding.webView)
                    }
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 12+ (API 31+) splash screen exit animation setup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                splashScreenView.remove()
            }
        }

        // Request POST_NOTIFICATIONS permission on Android 13+ (API 33+) for download notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        insetsController = WindowInsetsControllerCompat(window, window.decorView)

        // Automatic System Dark/Light Mode Detection (Matching Packora & System Appearance)
        isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val defaultBgColor = if (isNightMode) Color.parseColor("#121212") else Color.WHITE
        updateSystemBarsTheme(defaultBgColor, defaultBgColor)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())

            val density = resources.displayMetrics.density
            val isGestureNav = navBars.bottom < (24 * density).toInt()
            val bottomPadding = if (isGestureNav) 0 else navBars.bottom

            view.updatePadding(
                top = maxOf(statusBars.top, cutout.top),
                bottom = bottomPadding,
                left = maxOf(navBars.left, cutout.left),
                right = maxOf(navBars.right, cutout.right)
            )
            insets
        }

        config = loadConfig()
        setupWebView()

        val targetUrl = config?.optString("targetUrl", "")?.takeIf { it.isNotBlank() }
        val webViewConfig = config?.optJSONObject("webViewConfig")
        val browserEngine = webViewConfig?.optString("browserEngine", "SYSTEM_DEFAULT") ?: "SYSTEM_DEFAULT"

        if (targetUrl != null) {
            binding.webView.loadUrl(targetUrl)
        } else {
            binding.webView.loadData(
                "<html><body style='font-family:sans-serif;padding:24px;background:#1a1a2e;color:#fff;'>" +
                "<h2 style='color:#e94560;'>Configuration Error</h2>" +
                "<p>app_config.json was not found or has no targetUrl. APK injection may have failed.</p>" +
                "</body></html>",
                "text/html", "utf-8"
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isNightMode = (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        // Dynamically update WebView Force Dark / Algorithmic Darkening when system theme changes
        val settings = binding.webView.settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, isNightMode)
                } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    @Suppress("DEPRECATION")
                    WebSettingsCompat.setForceDark(
                        settings,
                        if (isNightMode) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
                    )
                }
            } catch (e: Exception) { }
        }

        syncWebPageThemeColor(binding.webView)
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun setupWebView() {
        val webView = binding.webView
        val settings = webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        @Suppress("DEPRECATION")
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.allowContentAccess = true
        settings.loadsImagesAutomatically = true

        // Configure System Cookie Manager for Shared Browser Cookies & Login Access
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }

        // Configure WebView Force Dark / Algorithmic Darkening matching System Dark Mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, isNightMode)
                } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    @Suppress("DEPRECATION")
                    WebSettingsCompat.setForceDark(
                        settings,
                        if (isNightMode) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
                    )
                }
            } catch (e: Exception) { }
        }

        // Enable Android Autofill Framework for Password Managers (Google Password Manager, Bitwarden, etc.)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES
        }
        webView.addJavascriptInterface(AutofillBridge(), "AndroidAutofill")

        val webViewConfig = config?.optJSONObject("webViewConfig")
        val isDesktopMode = webViewConfig?.optBoolean("desktopMode", false) ?: false
        val allowCopying = webViewConfig?.optBoolean("allowCopying", false) ?: false

        if (isDesktopMode) {
            val desktopUA = webViewConfig?.optString("userAgent")
                ?.takeIf { it.isNotBlank() }
                ?: "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"

            settings.userAgentString = desktopUA
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.defaultTextEncodingName = "utf-8"
        } else {
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
            settings.useWideViewPort = false
            settings.loadWithOverviewMode = false
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                syncWebPageThemeColor(view)
                if (isDesktopMode) {
                    view?.evaluateJavascript(
                        """
                        (function() {
                            var meta = document.querySelector('meta[name="viewport"]');
                            if (!meta) {
                                meta = document.createElement('meta');
                                meta.name = 'viewport';
                                (document.head || document.documentElement).appendChild(meta);
                            }
                            meta.setAttribute('content', 'width=1024, user-scalable=yes');
                        })();
                        """.trimIndent(), null
                    )
                }
                if (!allowCopying) {
                    injectCopyProtection(view)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                syncWebPageThemeColor(view)
                if (isDesktopMode) {
                    view?.evaluateJavascript(
                        """
                        (function() {
                            var meta = document.querySelector('meta[name="viewport"]');
                            if (meta) {
                                meta.setAttribute('content', 'width=1024, user-scalable=yes');
                            }
                        })();
                        """.trimIndent(), null
                    )
                }

                if (!allowCopying) {
                    injectCopyProtection(view)
                }

                // Inject Autofill & Password Manager helper logic into login forms
                view?.evaluateJavascript(
                    """
                    (function() {
                        try {
                            var inputs = document.querySelectorAll('input[type="text"], input[type="email"], input[type="password"]');
                            inputs.forEach(function(input) {
                                var type = input.getAttribute('type') || '';
                                var name = (input.getAttribute('name') || '').toLowerCase();
                                var id = (input.getAttribute('id') || '').toLowerCase();
                                
                                if (type === 'password' && !input.getAttribute('autocomplete')) {
                                    input.setAttribute('autocomplete', 'current-password');
                                } else if ((name.includes('login') || name.includes('user') || name.includes('email') || id.includes('login') || id.includes('user')) && !input.getAttribute('autocomplete')) {
                                    input.setAttribute('autocomplete', 'username');
                                }
                                
                                input.addEventListener('focus', function() {
                                    if (window.AndroidAutofill && window.AndroidAutofill.triggerAutofill) {
                                        window.AndroidAutofill.triggerAutofill();
                                    }
                                });
                            });
                        } catch(e) {}
                    })();
                    """.trimIndent(), null
                )
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                val openExternalLinks = webViewConfig?.optBoolean("openExternalLinks", true) ?: true
                val targetUrl = config?.optString("targetUrl", "") ?: ""

                if (openExternalLinks && targetUrl.isNotEmpty()) {
                    val targetHost = Uri.parse(targetUrl).host
                    val currentHost = Uri.parse(url).host
                    if (targetHost != null && currentHost != null &&
                        !currentHost.contains(targetHost) && !targetHost.contains(currentHost)
                    ) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    }
                }
                return false
            }
        }

        // Enable HTML5 File Input Chooser, WebRTC Camera/Microphone, Geolocation & Theme Color Sync
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                if (request != null) {
                    val requestedResources = request.resources
                    val permissionsToRequest = mutableListOf<String>()

                    for (resource in requestedResources) {
                        if (resource == android.webkit.PermissionRequest.RESOURCE_AUDIO_CAPTURE) {
                            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
                        } else if (resource == android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE) {
                            permissionsToRequest.add(Manifest.permission.CAMERA)
                        }
                    }

                    if (permissionsToRequest.isNotEmpty()) {
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            permissionsToRequest.toTypedArray(),
                            1001
                        )
                    }
                    request.grant(requestedResources)
                }
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: android.webkit.GeolocationPermissions.Callback?
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    1002
                )
                callback?.invoke(origin, true, false)
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback

                val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }

                return try {
                    fileChooserLauncher.launch(intent)
                    true
                } catch (e: Exception) {
                    this@MainActivity.filePathCallback = null
                    false
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                syncWebPageThemeColor(view)
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress > 25) {
                    syncWebPageThemeColor(view)
                }
            }
        }

        // Enable Download Manager with System Download Notifications
        webView.setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
            try {
                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setMimeType(mimetype)
                    addRequestHeader("User-Agent", userAgent)
                    setDescription("Downloading file...")
                    val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
                    setTitle(fileName)
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                }
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(applicationContext, "Download started...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun injectCopyProtection(webView: WebView?) {
        webView?.evaluateJavascript(
            """
            (function() {
                try {
                    var style = document.getElementById('packora-copy-protection');
                    if (!style) {
                        style = document.createElement('style');
                        style.id = 'packora-copy-protection';
                        style.innerHTML = '* { -webkit-user-select: none !important; user-select: none !important; -webkit-touch-callout: none !important; } input, textarea, [contenteditable="true"] { -webkit-user-select: text !important; user-select: text !important; }';
                        (document.head || document.documentElement).appendChild(style);
                    }
                    document.addEventListener('copy', function(e) {
                        var target = e.target;
                        if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable)) return;
                        e.preventDefault();
                    }, true);
                    document.addEventListener('contextmenu', function(e) {
                        var target = e.target;
                        if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable)) return;
                        e.preventDefault();
                    }, true);
                } catch(e) {}
            })();
            """.trimIndent(), null
        )
    }

    private fun syncWebPageThemeColor(view: WebView?) {
        view?.evaluateJavascript(
            """
            (function() {
                var meta = document.querySelector('meta[name="theme-color"]');
                var topColor = meta ? meta.content : '';
                function getBg(elem) {
                    if (!elem) return '';
                    var color = window.getComputedStyle(elem).backgroundColor;
                    if (color && color !== 'rgba(0, 0, 0, 0)' && color !== 'transparent') return color;
                    return '';
                }
                if (!topColor) {
                    var header = document.querySelector('header') || document.querySelector('nav') || document.querySelector('.header');
                    topColor = getBg(header) || getBg(document.body) || getBg(document.documentElement) || '';
                }
                var bottomColor = getBg(document.body) || getBg(document.documentElement) || topColor;
                return JSON.stringify({ top: topColor, bottom: bottomColor });
            })()
            """.trimIndent()
        ) { rawJson ->
            if (!rawJson.isNullOrBlank() && rawJson != "null") {
                try {
                    val cleanJson = rawJson.replace("^\"|\"$".toRegex(), "").replace("\\\"", "\"")
                    val jsonObj = JSONObject(cleanJson)
                    val topStr = jsonObj.optString("top", "")
                    val bottomStr = jsonObj.optString("bottom", "")

                    val topColor = parseCssColor(topStr)
                    val bottomColor = parseCssColor(bottomStr) ?: topColor

                    if (topColor != null) {
                        runOnUiThread {
                            updateSystemBarsTheme(topColor, bottomColor ?: topColor)
                        }
                    } else {
                        val fallbackColor = if (isNightMode) Color.parseColor("#121212") else Color.WHITE
                        runOnUiThread {
                            updateSystemBarsTheme(fallbackColor, fallbackColor)
                        }
                    }
                } catch (e: Exception) {
                    val fallbackColor = if (isNightMode) Color.parseColor("#121212") else Color.WHITE
                    runOnUiThread {
                        updateSystemBarsTheme(fallbackColor, fallbackColor)
                    }
                }
            } else {
                val fallbackColor = if (isNightMode) Color.parseColor("#121212") else Color.WHITE
                runOnUiThread {
                    updateSystemBarsTheme(fallbackColor, fallbackColor)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun updateSystemBarsTheme(topColor: Int, bottomColor: Int = topColor) {
        val isTopLight = isColorLight(topColor)
        val isBottomLight = isColorLight(bottomColor)

        // Status Bar Font & Icon Contrast:
        // Light background -> BLACK font & icons (isAppearanceLightStatusBars = true)
        // Dark background  -> WHITE font & icons (isAppearanceLightStatusBars = false)
        insetsController.isAppearanceLightStatusBars = isTopLight

        // Navigation Bar Button Contrast:
        // Light background -> BLACK navigation buttons (isAppearanceLightNavigationBars = true)
        // Dark background  -> WHITE navigation buttons (isAppearanceLightNavigationBars = false)
        insetsController.isAppearanceLightNavigationBars = isBottomLight

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = topColor
            window.navigationBarColor = bottomColor
        }

        window.decorView.setBackgroundColor(topColor)
        binding.root.setBackgroundColor(topColor)
        binding.webView.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun parseCssColor(cssStr: String): Int? {
        val clean = cssStr.replace("\"", "").trim()
        if (clean.startsWith("#")) {
            return try { Color.parseColor(clean) } catch (e: Exception) { null }
        }
        if (clean.startsWith("rgb")) {
            val match = Regex("""rgba?\((\d+),\s*(\d+),\s*(\d+)""").find(clean)
            if (match != null) {
                val (r, g, b) = match.destructured
                return try { Color.rgb(r.toInt(), g.toInt(), b.toInt()) } catch (e: Exception) { null }
            }
        }
        return null
    }

    private fun isColorLight(color: Int): Boolean {
        val red = Color.red(color) / 255.0
        val green = Color.green(color) / 255.0
        val blue = Color.blue(color) / 255.0
        val luminance = 0.2126 * red + 0.7152 * green + 0.0722 * blue
        return luminance > 0.5
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun loadConfig(): JSONObject? {
        return try {
            val inputStream = assets.open("app_config.json")
            val reader = InputStreamReader(inputStream)
            val jsonString = reader.readText()
            reader.close()
            JSONObject(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
