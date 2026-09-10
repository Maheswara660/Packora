package com.maheswara660.packora.template

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
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
import java.io.ByteArrayInputStream
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

    // JavaScript Bridge for native Android Password Manager / Autofill
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

    // JavaScript Bridge to allow WebApp push notifications to show as real Android System Bar Notifications
    inner class NotificationBridge {
        @JavascriptInterface
        fun showNotification(title: String, body: String, iconUrl: String?) {
            runOnUiThread {
                try {
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val channelId = "web_notifications_channel"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            channelId,
                            "Web Notifications",
                            NotificationManager.IMPORTANCE_HIGH
                        ).apply {
                            description = "Notifications from Web App"
                            enableLights(true)
                            enableVibration(true)
                        }
                        notificationManager.createNotificationChannel(channel)
                    }

                    val intent = Intent(this@MainActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        this@MainActivity,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
                    )

                    val appTitle = config?.optString("appName", "Notification") ?: "Notification"
                    val builder = NotificationCompat.Builder(this@MainActivity, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(if (title.isNotBlank()) title else appTitle)
                        .setContentText(body)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)

                    val notificationId = (System.currentTimeMillis() % 10000).toInt()
                    notificationManager.notify(notificationId, builder.build())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 12+ splash screen exit animation setup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                splashScreenView.remove()
            }
        }

        // Create Notification Channel for Web Notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "web_notifications_channel",
                "Web Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications from Web App"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Request POST_NOTIFICATIONS permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        insetsController = WindowInsetsControllerCompat(window, window.decorView)

        // Modern OnBackPressedDispatcher handling for Web History Routing
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // System Dark/Light Mode Detection
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

        binding.btnRetry.setOnClickListener {
            hideErrorOverlay()
            val targetUrl = config?.optString("targetUrl", "")?.takeIf { it.isNotBlank() }
            if (targetUrl != null) {
                binding.webView.loadUrl(targetUrl)
            } else {
                binding.webView.reload()
            }
        }

        val targetUrl = config?.optString("targetUrl", "")?.takeIf { it.isNotBlank() }

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

    private fun showErrorOverlay() {
        runOnUiThread {
            val bg = if (isNightMode) Color.parseColor("#121212") else Color.parseColor("#F8FAFC")
            binding.errorOverlay.setBackgroundColor(bg)
            binding.errorOverlay.visibility = View.VISIBLE
            binding.webView.visibility = View.GONE
        }
    }

    private fun hideErrorOverlay() {
        runOnUiThread {
            binding.errorOverlay.visibility = View.GONE
            binding.webView.visibility = View.VISIBLE
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isNightMode = (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

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
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.setSupportMultipleWindows(true)

        // Configure Cookie Manager
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }

        // Force Dark / Algorithmic Darkening
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

        // Enable Password Managers & Web Notifications Bridges
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES
        }
        webView.addJavascriptInterface(AutofillBridge(), "AndroidAutofill")
        webView.addJavascriptInterface(NotificationBridge(), "AndroidNotification")

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
            // Clean standard Chrome User Agent so Google OAuth work seamlessly
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
            settings.useWideViewPort = false
            settings.loadWithOverviewMode = false
        }

        webView.webViewClient = object : WebViewClient() {

            // AdBlocker Request Interception
            override fun shouldInterceptRequest(
                view: WebView?, request: WebResourceRequest?
            ): WebResourceResponse? {
                val requestUrl = request?.url?.toString()
                if (isAdOrGamblingUrl(requestUrl)) {
                    // Block ad & tracking requests by returning empty 0-byte stream
                    return WebResourceResponse(
                        "text/plain",
                        "UTF-8",
                        ByteArrayInputStream(ByteArray(0))
                    )
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                hideErrorOverlay()
                syncWebPageThemeColor(view)
                injectAdBlockerAndSpaceCollapsing(view)
                injectNotificationPolyfill(view)

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

                // Block window.open ad/gambling popunder redirects
                view?.evaluateJavascript(
                    """
                    (function() {
                        try {
                            var origOpen = window.open;
                            window.open = function(u, t, f) {
                                if (u && (
                                    u.includes('parimatch') || u.includes('1xbet') || u.includes('bet365') || 
                                    u.includes('popads') || u.includes('popcash') || u.includes('adsterra') ||
                                    u.includes('propellerads') || u.includes('exoclick') || u.includes('onclick') ||
                                    u.includes('googlesyndication') || u.includes('doubleclick')
                                )) {
                                    return null;
                                }
                                return origOpen.apply(this, arguments);
                            };
                        } catch(e) {}
                    })();
                    """.trimIndent(), null
                )
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                syncWebPageThemeColor(view)
                injectAdBlockerAndSpaceCollapsing(view)
                injectNotificationPolyfill(view)

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

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    showErrorOverlay()
                }
            }

            @Suppress("DEPRECATION")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                showErrorOverlay()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false

                // 1. Block ad, popunder, and gambling promotional redirect links
                if (isAdOrGamblingUrl(url)) {
                    return true // Intercept & block navigation to ad/gambling sites
                }

                val uri = Uri.parse(url)
                val scheme = uri.scheme?.lowercase() ?: ""

                // Handle non-http(s) custom URI schemes (mailto, tel, whatsapp, intent)
                if (scheme != "http" && scheme != "https") {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(intent)
                        return true
                    } catch (e: Exception) {
                        return true
                    }
                }

                // 2. Deep Linking to Popular Installed Native Apps (YouTube, LinkedIn, Instagram, Twitter, etc.)
                if (tryLaunchInInstalledNativeApp(url)) {
                    return true
                }

                val openExternalLinks = webViewConfig?.optBoolean("openExternalLinks", false) ?: false
                val targetUrl = config?.optString("targetUrl", "") ?: ""

                if (openExternalLinks && targetUrl.isNotEmpty()) {
                    val targetHost = Uri.parse(targetUrl).host?.lowercase()
                    val currentHost = uri.host?.lowercase()

                    val isAuthOrRedirect = currentHost != null && (
                        currentHost.contains("accounts.google") ||
                        currentHost.contains("login") ||
                        currentHost.contains("oauth") ||
                        currentHost.contains("auth") ||
                        currentHost.contains("sso") ||
                        currentHost.contains("identity") ||
                        currentHost.contains("recaptcha")
                    )

                    val isSameDomainFamily = targetHost != null && currentHost != null && (
                        currentHost.endsWith(targetHost) || targetHost.endsWith(currentHost) ||
                        currentHost.split(".").takeLast(2) == targetHost.split(".").takeLast(2)
                    )

                    // Keep intra-site links inside WebView; open distinct external domains in Custom Tabs
                    if (!isSameDomainFamily && !isAuthOrRedirect && request?.isForMainFrame == true && !request.isRedirect) {
                        try {
                            val customTabsIntent = CustomTabsIntent.Builder().build()
                            customTabsIntent.launchUrl(this@MainActivity, uri)
                            return true
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                startActivity(intent)
                                return true
                            } catch (ex: Exception) { }
                        }
                    }
                }

                // Keep all intra-site navigation inside WebView
                return false
            }
        }

        // Enable HTML5 File Input Chooser, WebRTC Camera/Microphone, Geolocation, Notifications & Window Popup Interception
        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?
            ): Boolean {
                val hit = view?.hitTestResult
                val popupUrl = hit?.extra
                if (isAdOrGamblingUrl(popupUrl)) {
                    return false // Intercept & block ad/gambling popup windows
                }

                // Route intra-site or popup windows into main WebView
                val transport = resultMsg?.obj as? WebView.WebViewTransport
                val tempWebView = WebView(this@MainActivity)
                tempWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(v: WebView?, req: WebResourceRequest?): Boolean {
                        val target = req?.url?.toString() ?: return false
                        if (isAdOrGamblingUrl(target)) {
                            return true
                        }
                        if (tryLaunchInInstalledNativeApp(target)) {
                            return true
                        }
                        binding.webView.loadUrl(target)
                        return true
                    }
                }
                transport?.webView = tempWebView
                resultMsg?.sendToTarget()
                return true
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress > 25) {
                    syncWebPageThemeColor(view)
                }
            }

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

                val intent = try {
                    fileChooserParams?.createIntent()?.apply {
                        if (fileChooserParams.mode == FileChooserParams.MODE_OPEN_MULTIPLE) {
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        }
                    }
                } catch (e: Exception) { null } ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    if (fileChooserParams?.mode == FileChooserParams.MODE_OPEN_MULTIPLE) {
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    }
                }

                return try {
                    fileChooserLauncher.launch(intent)
                    true
                } catch (e: Exception) {
                    this@MainActivity.filePathCallback?.onReceiveValue(null)
                    this@MainActivity.filePathCallback = null
                    false
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                syncWebPageThemeColor(view)
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

    private fun tryLaunchInInstalledNativeApp(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            val scheme = uri.scheme?.lowercase() ?: ""

            // Handle intent:// URI schemes with fallback URL support
            if (scheme == "intent") {
                try {
                    val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    if (intent != null) {
                        val info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                        if (info != null) {
                            startActivity(intent)
                            return true
                        }
                        val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                        if (!fallbackUrl.isNullOrBlank()) {
                            binding.webView.loadUrl(fallbackUrl)
                            return true
                        }
                    }
                } catch (e: Exception) { }
                return true
            }

            // Check if ANY installed native app or Packora-created WebAPK handles this link/domain
            if (scheme == "http" || scheme == "https") {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                val resolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

                val browserPackages = setOf(
                    "com.android.chrome", "org.mozilla.firefox", "com.sec.android.app.sbrowser",
                    "com.opera.browser", "com.microsoft.emmx", "com.brave.browser", "com.duckduckgo.mobile.android",
                    "com.android.browser", packageName
                )

                val matchingApp = resolveInfoList.firstOrNull { info ->
                    val pkg = info.activityInfo.packageName
                    pkg !in browserPackages && !pkg.contains("browser")
                }

                if (matchingApp != null) {
                    intent.setPackage(matchingApp.activityInfo.packageName)
                    startActivity(intent)
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun injectAdBlockerAndSpaceCollapsing(webView: WebView?) {
        webView?.evaluateJavascript(
            """
            (function() {
                try {
                    var styleId = 'packora-adblock-styles';
                    if (!document.getElementById(styleId)) {
                        var style = document.createElement('style');
                        style.id = styleId;
                        style.innerHTML = `
                            ins.adsbygoogle, [class*="ad-"], [id*="ad-"], [class*="banner"], [id*="banner"],
                            iframe[src*="doubleclick"], iframe[src*="googlesyndication"], iframe[src*="taboola"], iframe[src*="outbrain"], iframe[src*="adsterra"],
                            .ad-container, .ad-wrapper, .ad-slot, .ad-unit, .sponsored-content, .pubnation-ad, .adbox, #adbox, .ad_box, #ad_box {
                                display: none !important;
                                height: 0 !important;
                                max-height: 0 !important;
                                min-height: 0 !important;
                                margin: 0 !important;
                                padding: 0 !important;
                                visibility: hidden !important;
                                opacity: 0 !important;
                                pointer-events: none !important;
                                overflow: hidden !important;
                            }
                        `;
                        (document.head || document.documentElement).appendChild(style);
                    }

                    var cleanAdElements = function() {
                        var selectors = [
                            'ins.adsbygoogle', 'iframe[src*="doubleclick"]', 'iframe[src*="googlesyndication"]',
                            'iframe[src*="taboola"]', 'iframe[src*="outbrain"]', 'iframe[src*="adsterra"]',
                            '.ad-container', '.ad-wrapper', '.ad-slot', '.ad-unit', '.sponsored-content'
                        ];
                        selectors.forEach(function(s) {
                            document.querySelectorAll(s).forEach(function(el) {
                                el.style.display = 'none';
                                el.style.height = '0px';
                            });
                        });
                    };
                    cleanAdElements();
                    var observer = new MutationObserver(cleanAdElements);
                    observer.observe(document.body || document.documentElement, { childList: true, subtree: true });
                } catch(e) {}
            })();
            """.trimIndent(), null
        )
    }

    private fun injectNotificationPolyfill(webView: WebView?) {
        webView?.evaluateJavascript(
            """
            (function() {
                try {
                    if (!window.Notification) {
                        window.Notification = function(title, options) {
                            options = options || {};
                            var body = options.body || '';
                            var icon = options.icon || '';
                            if (window.AndroidNotification && window.AndroidNotification.showNotification) {
                                window.AndroidNotification.showNotification(title, body, icon);
                            }
                        };
                        window.Notification.permission = 'granted';
                        window.Notification.requestPermission = function(cb) {
                            if (cb) cb('granted');
                            return Promise.resolve('granted');
                        };
                    } else {
                        window.Notification.permission = 'granted';
                        var origReq = window.Notification.requestPermission;
                        window.Notification.requestPermission = function(cb) {
                            if (cb) cb('granted');
                            return Promise.resolve('granted');
                        };
                    }
                } catch(e) {}
            })();
            """.trimIndent(), null
        )
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

        insetsController.isAppearanceLightStatusBars = isTopLight
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

    private fun isAdOrGamblingUrl(urlString: String?): Boolean {
        if (urlString.isNullOrBlank()) return false
        val lower = urlString.lowercase()
        val blacklistedKeywords = listOf(
            "parimatch", "1xbet", "bet365", "popads", "popcash", "adsterra", "propellerads",
            "exoclick", "betway", "stake.com", "dafabet", "mostbet", "adroll", "doubleclick",
            "googlesyndication", "onclickads", "bet9ja", "melbet", "win100", "slot", "casino",
            "gambling", "betting", "affiliate", "redirectad", "ad-delivery", "adserver",
            "fastclick", "revenuehits", "flyout", "ad-click", "click-redirect", "taboola", "outbrain",
            "adnxs.com", "criteo.com", "amazon-adsystem.com", "rubiconproject.com"
        )
        return blacklistedKeywords.any { lower.contains(it) }
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
