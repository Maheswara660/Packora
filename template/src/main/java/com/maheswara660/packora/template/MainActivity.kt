package com.maheswara660.packora.template

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
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
import android.view.KeyEvent
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
import android.webkit.WebSettings
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
import androidx.credentials.CreateCredentialResponse
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
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
    private var hideWebFooter: Boolean = false
    private lateinit var webView: WebView

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

    // JavaScript Bridge for Passkey (WebAuthn / FIDO2) via Android CredentialManager
    inner class PasskeyBridge {
        private val credentialManager by lazy { CredentialManager.create(this@MainActivity) }

        @JavascriptInterface
        fun isSupported(): Boolean = true

        @JavascriptInterface
        fun getCredential(requestId: String, requestJson: String) {
            runOnUiThread {
                try {
                    val getOption = GetPublicKeyCredentialOption(requestJson)
                    val getRequest = GetCredentialRequest(listOf(getOption))
                    credentialManager.getCredentialAsync(
                        context = this@MainActivity,
                        request = getRequest,
                        cancellationSignal = null,
                        executor = ContextCompat.getMainExecutor(this@MainActivity),
                        callback = object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                            override fun onResult(result: GetCredentialResponse) {
                                val cred = result.credential
                                if (cred is PublicKeyCredential) {
                                    resolvePasskey(requestId, cred.authenticationResponseJson)
                                } else {
                                    rejectPasskey(requestId, "Unsupported credential type")
                                }
                            }

                            override fun onError(e: GetCredentialException) {
                                rejectPasskey(requestId, e.message ?: "Authentication failed")
                            }
                        }
                    )
                } catch (e: Exception) {
                    rejectPasskey(requestId, e.message ?: "Passkey error")
                }
            }
        }

        @JavascriptInterface
        fun createCredential(requestId: String, requestJson: String) {
            runOnUiThread {
                try {
                    val createRequest = CreatePublicKeyCredentialRequest(requestJson)
                    credentialManager.createCredentialAsync(
                        context = this@MainActivity,
                        request = createRequest,
                        cancellationSignal = null,
                        executor = ContextCompat.getMainExecutor(this@MainActivity),
                        callback = object : CredentialManagerCallback<CreateCredentialResponse, CreateCredentialException> {
                            override fun onResult(result: CreateCredentialResponse) {
                                if (result is CreatePublicKeyCredentialResponse) {
                                    resolvePasskey(requestId, result.registrationResponseJson)
                                } else {
                                    rejectPasskey(requestId, "Unsupported registration result")
                                }
                            }

                            override fun onError(e: CreateCredentialException) {
                                rejectPasskey(requestId, e.message ?: "Registration failed")
                            }
                        }
                    )
                } catch (e: Exception) {
                    rejectPasskey(requestId, e.message ?: "Passkey creation error")
                }
            }
        }

        private fun resolvePasskey(requestId: String, responseJson: String) {
            val escaped = JSONObject.quote(responseJson)
            binding.webView.evaluateJavascript("window.__packoraPasskeyResolve && window.__packoraPasskeyResolve('$requestId', $escaped);", null)
        }

        private fun rejectPasskey(requestId: String, errorMsg: String) {
            val escaped = JSONObject.quote(errorMsg)
            binding.webView.evaluateJavascript("window.__packoraPasskeyReject && window.__packoraPasskeyReject('$requestId', $escaped);", null)
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
        webView = binding.webView

        binding.btnRetry.setOnClickListener {
            hideErrorOverlay()
            val targetUrl = config?.optString("targetUrl", "")?.takeIf { it.isNotBlank() }
            if (targetUrl != null) {
                binding.webView.loadUrl(targetUrl)
            } else {
                binding.webView.reload()
            }
        }

        val deepLinkUrl = intent.data?.toString()?.takeIf { it.isNotBlank() }
        val targetUrl = deepLinkUrl ?: config?.optString("targetUrl", "")?.takeIf { it.isNotBlank() }

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

    override fun onPause() {
        super.onPause()
        try { CookieManager.getInstance().flush() } catch (e: Exception) {}
    }

    override fun onStop() {
        super.onStop()
        try { CookieManager.getInstance().flush() } catch (e: Exception) {}
    }

    // Handle OAuth redirect callbacks (e.g. https://app.example.com/callback)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val redirectUrl = intent.data?.toString()
        if (!redirectUrl.isNullOrBlank() && ::webView.isInitialized) {
            webView.loadUrl(redirectUrl)
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
        settings.setSupportMultipleWindows(true)
        settings.javaScriptCanOpenWindowsAutomatically = true

        // Universal Cookie & Session Acceptance (First-party + Third-party)
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        val webViewConfig = config?.optJSONObject("webViewConfig")
        val isDesktopMode = webViewConfig?.optBoolean("desktopMode", false) ?: false
        val allowCopying = webViewConfig?.optBoolean("allowCopying", false) ?: false
        val forceDarkMode = webViewConfig?.optBoolean("forceDarkMode", false) ?: false
        val enableZoom = webViewConfig?.optBoolean("enableZoom", false) ?: false
        hideWebFooter = webViewConfig?.optBoolean("hideWebFooter", false) ?: false

        settings.setSupportZoom(enableZoom)
        settings.builtInZoomControls = enableZoom
        settings.displayZoomControls = false

        // Force Dark / Algorithmic Darkening (Only if explicitly enabled by user)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, forceDarkMode && isNightMode)
                } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    @Suppress("DEPRECATION")
                    WebSettingsCompat.setForceDark(
                        settings,
                        if (forceDarkMode && isNightMode) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
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
        webView.addJavascriptInterface(PasskeyBridge(), "AndroidPasskey")

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
                injectPasskeyPolyfill(view)
                injectAutoAcceptCookies(view)
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
                try { CookieManager.getInstance().flush() } catch (e: Exception) {}
                syncWebPageThemeColor(view)
                injectPasskeyPolyfill(view)
                injectAutoAcceptCookies(view)
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

                if (hideWebFooter) {
                    injectWebFooterHider(view)
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

                // Handle payment gateways and non-http(s) custom URI schemes
                val paymentSchemes = setOf("upi", "tez", "phonepe", "paytmmp", "bhim", "gpay", "paypal", "swish", "venmo", "cred")
                if (scheme in paymentSchemes) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                            return true
                        } else {
                            Toast.makeText(this@MainActivity, "No payment app found for $scheme", Toast.LENGTH_SHORT).show()
                            return true
                        }
                    } catch (e: Exception) {
                        return true
                    }
                }

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
                        // Google, Apple, Microsoft
                        currentHost.contains("accounts.google") ||
                        currentHost.contains("login.microsoftonline") ||
                        currentHost.contains("appleid.apple") ||
                        currentHost.contains("auth.apple") ||
                        currentHost.contains("signin.aws") ||
                        // Enterprise SSO / Identity platforms
                        currentHost.contains("cognito") ||
                        currentHost.contains("auth0") ||
                        currentHost.contains("okta") ||
                        currentHost.contains("onelogin") ||
                        currentHost.contains("pingidentity") ||
                        currentHost.contains("ping.identity") ||
                        currentHost.contains("sailpoint") ||
                        currentHost.contains("forgerock") ||
                        currentHost.contains("keycloak") ||
                        // Clerk (used by Notion, Linear, Loom, etc.)
                        currentHost.contains("clerk.") ||
                        currentHost.contains("clerkstage") ||
                        currentHost.contains("clerkdev") ||
                        // Auth keywords in subdomain or path
                        currentHost.contains(".auth.") ||
                        currentHost.startsWith("auth.") ||
                        currentHost.contains("oauth") ||
                        currentHost.contains("openid") ||
                        currentHost.contains("oidc") ||
                        currentHost.startsWith("login.") ||
                        currentHost.startsWith("signin.") ||
                        currentHost.startsWith("signup.") ||
                        currentHost.startsWith("register.") ||
                        currentHost.startsWith("sso.") ||
                        currentHost.contains("saml") ||
                        currentHost.contains("ldap") ||
                        currentHost.startsWith("identity.") ||
                        currentHost.startsWith("id.") ||
                        currentHost.contains(".iam.") ||
                        currentHost.contains("federat") ||
                        currentHost.startsWith("connect.") ||
                        currentHost.startsWith("token.") ||
                        currentHost.startsWith("authorize.") ||
                        currentHost.startsWith("callback.") ||
                        currentHost.contains("redirect") ||
                        // Modern auth-as-a-service
                        currentHost.contains("firebaseapp") ||
                        currentHost.contains("supabase.co") ||
                        currentHost.contains("workos") ||
                        currentHost.contains("stytch") ||
                        currentHost.contains("descope") ||
                        currentHost.contains("magic.link") ||
                        currentHost.contains("passwordless") ||
                        // MFA / OTP / Security verification
                        currentHost.contains("duo.com") ||
                        currentHost.contains("recaptcha") ||
                        currentHost.contains("hcaptcha") ||
                        currentHost.contains("turnstile") ||
                        currentHost.startsWith("verify.") ||
                        currentHost.startsWith("2fa.") ||
                        currentHost.startsWith("mfa.") ||
                        currentHost.startsWith("otp.") ||
                        currentHost.startsWith("secure.") ||
                        // Popular platform OAuth endpoints
                        (currentHost.contains("github.com") && url.contains("/login")) ||
                        (currentHost.contains("gitlab.com") && url.contains("/sign_in")) ||
                        (currentHost.contains("linkedin.com") && url.contains("/oauth")) ||
                        (currentHost.contains("facebook.com") && url.contains("/dialog")) ||
                        (currentHost.contains("twitter.com") && url.contains("/oauth")) ||
                        (currentHost.contains("discord.com") && url.contains("/oauth2")) ||
                        (currentHost.contains("slack.com") && url.contains("/oauth")) ||
                        (currentHost.contains("atlassian") && url.contains("/login")) ||
                        currentHost.contains("sso") ||
                        currentHost.contains("identity")
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

                val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false

                val popupWebView = WebView(this@MainActivity).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    @Suppress("DEPRECATION")
                    settings.databaseEnabled = true
                    settings.setSupportMultipleWindows(true)
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                }

                val popupDialog = Dialog(this@MainActivity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen).apply {
                    setContentView(popupWebView)
                    setOnDismissListener {
                        try {
                            popupWebView.stopLoading()
                            popupWebView.destroy()
                        } catch (e: Exception) {}
                    }
                    setOnKeyListener { _, keyCode, event ->
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                            if (popupWebView.canGoBack()) {
                                popupWebView.goBack()
                                true
                            } else {
                                dismiss()
                                true
                            }
                        } else false
                    }
                }

                popupWebView.webChromeClient = object : WebChromeClient() {
                    override fun onCloseWindow(window: WebView?) {
                        try { popupDialog.dismiss() } catch (e: Exception) {}
                    }
                }

                popupWebView.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(v: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(v, url, favicon)
                        injectPasskeyPolyfill(v)
                        injectAutoAcceptCookies(v)
                    }

                    override fun onPageFinished(v: WebView?, url: String?) {
                        super.onPageFinished(v, url)
                        try { CookieManager.getInstance().flush() } catch (e: Exception) {}
                        injectPasskeyPolyfill(v)
                        injectAutoAcceptCookies(v)
                    }

                    override fun shouldOverrideUrlLoading(v: WebView?, req: WebResourceRequest?): Boolean {
                        val target = req?.url?.toString() ?: return false
                        if (isAdOrGamblingUrl(target)) {
                            return true
                        }
                        if (tryLaunchInInstalledNativeApp(target)) {
                            try { popupDialog.dismiss() } catch (e: Exception) {}
                            return true
                        }
                        return false // Let popupWebView navigate internally
                    }
                }

                transport.webView = popupWebView
                resultMsg.sendToTarget()
                try { popupDialog.show() } catch (e: Exception) {}
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

        // Universal Download: cookies, correct filename, blob/data URL support
        webView.setDownloadListener(DownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
            // Handle blob: URLs via injected JS bridge
            if (url.startsWith("blob:") || url.startsWith("data:")) {
                webView.evaluateJavascript(
                    """(function() {
                        var url = '$url';
                        var a = document.createElement('a');
                        a.href = url;
                        a.download = 'download';
                        document.body.appendChild(a);
                        a.click();
                        document.body.removeChild(a);
                    })();""", null
                )
                return@DownloadListener
            }
            try {
                // Parse real filename from Content-Disposition (RFC 6266 + RFC 5987)
                val fileName: String = run {
                    // Try filename*=UTF-8''encoded (RFC 5987)
                    val rfc5987 = Regex("""filename\*\s*=\s*UTF-8''([^;\r\n]+)""", RegexOption.IGNORE_CASE)
                        .find(contentDisposition)?.groupValues?.get(1)
                        ?.let { java.net.URLDecoder.decode(it.trim(), "UTF-8") }
                    if (!rfc5987.isNullOrBlank()) return@run rfc5987
                    // Try filename="..."
                    val quoted = Regex("""filename\s*=\s*"([^"]+)"""", RegexOption.IGNORE_CASE)
                        .find(contentDisposition)?.groupValues?.get(1)?.trim()
                    if (!quoted.isNullOrBlank()) return@run quoted
                    // Try filename=... (unquoted)
                    val unquoted = Regex("""filename\s*=\s*([^;\r\n"\s]+)""", RegexOption.IGNORE_CASE)
                        .find(contentDisposition)?.groupValues?.get(1)?.trim()
                    if (!unquoted.isNullOrBlank() && !unquoted.equals("index.php", true)) return@run unquoted
                    // Last resort: URLUtil (will avoid index.php via URL path)
                    val guessed = URLUtil.guessFileName(url, contentDisposition, mimetype)
                    if (guessed.equals("index.php", true) || guessed.equals("downloadfile.php", true)) {
                        // Fallback: use domain + timestamp
                        val host = Uri.parse(url).host?.replace(".", "_") ?: "file"
                        val ext = when {
                            mimetype.contains("pdf") -> ".pdf"
                            mimetype.contains("zip") -> ".zip"
                            mimetype.contains("image") -> ".jpg"
                            mimetype.contains("video") -> ".mp4"
                            mimetype.contains("audio") -> ".mp3"
                            else -> ""
                        }
                        return@run "${host}_${System.currentTimeMillis()}$ext"
                    }
                    guessed
                }

                val cookies = CookieManager.getInstance().getCookie(url)
                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setMimeType(mimetype)
                    addRequestHeader("User-Agent", userAgent)
                    if (!cookies.isNullOrBlank()) addRequestHeader("Cookie", cookies)
                    val referer = webView.url
                    if (!referer.isNullOrBlank()) addRequestHeader("Referer", referer)
                    addRequestHeader("Accept", "*/*")
                    setTitle(fileName)
                    setDescription("Downloading $fileName")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    setAllowedOverMetered(true)
                    setAllowedOverRoaming(true)
                }
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(applicationContext, "Downloading $fileName", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Last fallback: open in browser
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (ex: Exception) {
                    Toast.makeText(applicationContext, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
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

            // Handle Payment Gateway Schemes (UPI, Google Pay, PhonePe, Paytm, BHIM, PayPal, etc.)
            val paymentSchemes = setOf(
                "upi", "tez", "phonepe", "paytmmp", "bhim", "gpay", "paypal", "swish", "venmo", "cred"
            )
            if (scheme in paymentSchemes) {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                    return true
                } else {
                    Toast.makeText(this@MainActivity, "No payment app found for $scheme", Toast.LENGTH_SHORT).show()
                    return true
                }
            }

            // Check if ANY installed native app or Packora-created WebAPK handles this link/domain
            if (scheme == "http" || scheme == "https") {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                val resolveInfoList = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.queryIntentActivities(intent, 0)
                    }
                } catch (e: Exception) {
                    @Suppress("DEPRECATION")
                    packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                }

                val browserPackages = setOf(
                    "com.android.chrome", "org.mozilla.firefox", "com.sec.android.app.sbrowser",
                    "com.opera.browser", "com.microsoft.emmx", "com.brave.browser", "com.duckduckgo.mobile.android",
                    "com.android.browser", packageName
                )

                val matchingApp = resolveInfoList.firstOrNull { info ->
                    val pkg = info.activityInfo.packageName
                    pkg != packageName && pkg !in browserPackages && !pkg.contains("browser")
                }

                if (matchingApp != null) {
                    intent.setPackage(matchingApp.activityInfo.packageName)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun injectPasskeyPolyfill(webView: WebView?) {
        webView?.evaluateJavascript(
            """
            (function() {
                try {
                    if (!window.AndroidPasskey) return;

                    if (!window.PublicKeyCredential) {
                        window.PublicKeyCredential = function() {};
                    }
                    window.PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable = function() {
                        return Promise.resolve(true);
                    };
                    window.PublicKeyCredential.isConditionalMediationAvailable = function() {
                        return Promise.resolve(true);
                    };

                    window.__packoraPasskeyCallbacks = window.__packoraPasskeyCallbacks || {};
                    window.__packoraPasskeyResolve = function(id, resJson) {
                        var cb = window.__packoraPasskeyCallbacks[id];
                        if (cb) {
                            delete window.__packoraPasskeyCallbacks[id];
                            try {
                                var parsed = typeof resJson === 'string' ? JSON.parse(resJson) : resJson;
                                cb.resolve(parsed);
                            } catch(e) {
                                cb.reject(e);
                            }
                        }
                    };
                    window.__packoraPasskeyReject = function(id, err) {
                        var cb = window.__packoraPasskeyCallbacks[id];
                        if (cb) {
                            delete window.__packoraPasskeyCallbacks[id];
                            cb.reject(new Error(err || 'Passkey failed'));
                        }
                    };

                    if (navigator.credentials) {
                        var origGet = navigator.credentials.get.bind(navigator.credentials);
                        navigator.credentials.get = function(options) {
                            if (options && options.publicKey && window.AndroidPasskey) {
                                return new Promise(function(resolve, reject) {
                                    var id = 'passkey_get_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
                                    window.__packoraPasskeyCallbacks[id] = { resolve: resolve, reject: reject };
                                    try {
                                        var jsonStr = JSON.stringify(options);
                                        window.AndroidPasskey.getCredential(id, jsonStr);
                                    } catch(e) {
                                        delete window.__packoraPasskeyCallbacks[id];
                                        reject(e);
                                    }
                                });
                            }
                            return origGet(options);
                        };

                        var origCreate = navigator.credentials.create.bind(navigator.credentials);
                        navigator.credentials.create = function(options) {
                            if (options && options.publicKey && window.AndroidPasskey) {
                                return new Promise(function(resolve, reject) {
                                    var id = 'passkey_create_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
                                    window.__packoraPasskeyCallbacks[id] = { resolve: resolve, reject: reject };
                                    try {
                                        var jsonStr = JSON.stringify(options);
                                        window.AndroidPasskey.createCredential(id, jsonStr);
                                    } catch(e) {
                                        delete window.__packoraPasskeyCallbacks[id];
                                        reject(e);
                                    }
                                });
                            }
                            return origCreate(options);
                        };
                    }
                } catch(e) {}
            })();
            """.trimIndent(), null
        )
    }

    private fun injectAutoAcceptCookies(webView: WebView?) {
        webView?.evaluateJavascript(
            """
            (function() {
                try {
                    // 1. Pre-approve Global CMP / Consent framework APIs
                    function triggerCmpApis() {
                        try {
                            if (window.OneTrust && typeof window.OneTrust.AllowAll === 'function') {
                                window.OneTrust.AllowAll();
                            }
                            if (window.Optanon && typeof window.Optanon.AllowAll === 'function') {
                                window.Optanon.AllowAll();
                            }
                            if (window.Cookiebot) {
                                if (window.Cookiebot.dialog && typeof window.Cookiebot.dialog.submitConsent === 'function') {
                                    window.Cookiebot.dialog.submitConsent(true, true, true);
                                }
                                if (window.Cookiebot.consent) {
                                    window.Cookiebot.consent.preferences = true;
                                    window.Cookiebot.consent.statistics = true;
                                    window.Cookiebot.consent.marketing = true;
                                }
                            }
                            if (typeof window.__tcfapi === 'function') {
                                window.__tcfapi('acceptAll', 2, function(){});
                                window.__tcfapi('saveAndExit', 2, function(){});
                            }
                            if (typeof window.__cmp === 'function') {
                                window.__cmp('acceptAll');
                                window.__cmp('saveAndExit');
                            }
                            if (window.Didomi && typeof window.Didomi.setUserAgreeToAll === 'function') {
                                window.Didomi.setUserAgreeToAll();
                            }
                            if (window.UC_UI && typeof window.UC_UI.acceptAllConsents === 'function') {
                                window.UC_UI.acceptAllConsents();
                            }
                            if (window.klaro && window.klaro.getManager) {
                                var mgr = window.klaro.getManager();
                                if (mgr && typeof mgr.changeAll === 'function') mgr.changeAll(true);
                                if (mgr && typeof mgr.saveConsents === 'function') mgr.saveConsents();
                            }
                            if (typeof window.gtag === 'function') {
                                window.gtag('consent', 'update', {
                                    'ad_storage': 'granted',
                                    'ad_user_data': 'granted',
                                    'ad_personalization': 'granted',
                                    'analytics_storage': 'granted',
                                    'functionality_storage': 'granted',
                                    'personalization_storage': 'granted',
                                    'security_storage': 'granted'
                                });
                            }
                        } catch(e) {}
                    }
                    triggerCmpApis();

                    // 2. Set common cookie consent tokens to preemptively satisfy checks
                    function setConsentTokens() {
                        try {
                            var future = new Date(Date.now() + 365*24*60*60*1000).toUTCString();
                            var host = location.hostname ? location.hostname.replace(/^www\./, '') : '';
                            var domainPart = host ? ';domain=.' + host : '';
                            var cEnd = ';expires=' + future + ';path=/' + domainPart + ';SameSite=Lax';

                            var commonCookies = [
                                'cookieconsent_status=dismiss',
                                'cookieconsent_status=allow',
                                'cookies_accepted=1',
                                'cookie_accepted=true',
                                'accept_cookies=1',
                                'cookie_consent=1',
                                'cookie_notice_accepted=true',
                                'eu_consent=true',
                                'gdpr_consent=1',
                                'privacy_consent=1',
                                'viewed_cookie_policy=yes'
                            ];
                            commonCookies.forEach(function(c) {
                                try { document.cookie = c + cEnd; } catch(e) {}
                            });

                            var localItems = {
                                'cookieconsent_status': 'allow',
                                'cookies_accepted': 'true',
                                'cookie-consent': 'accepted',
                                'gdpr_consent': 'accepted',
                                'accepted_cookies': 'true'
                            };
                            for (var key in localItems) {
                                try {
                                    if (!localStorage.getItem(key)) {
                                        localStorage.setItem(key, localItems[key]);
                                    }
                                } catch(e) {}
                            }
                        } catch(e) {}
                    }
                    setConsentTokens();

                    // 3. Known Cookie Banner Accept Button Selectors
                    var knownSelectors = [
                        '#onetrust-accept-btn-handler',
                        '#accept-recommended-btn-handler',
                        '#CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll',
                        '#CybotCookiebotDialogBodyButtonAccept',
                        '#didomi-notice-agree-button',
                        '.didomi-components-button--color-primary',
                        '#cookie_action_close_header',
                        '#cookie_action_close_header_accept',
                        '.cookie-notice-button-accept',
                        '#cookie-notice-accept',
                        '.cc-btn.cc-allow',
                        '.cc-allow',
                        '.cc-accept',
                        '.cc-dismiss',
                        'button[data-cookiefirst-action="accept"]',
                        'button[data-cy="accept-all-cookies"]',
                        'button[data-testid*="cookie-accept"]',
                        'button[data-testid*="accept-all"]',
                        'button[data-testid*="accept-cookie"]',
                        '[data-action="accept-all"]',
                        '[data-action="accept-cookies"]',
                        '[data-action="agree"]',
                        '#gdpr-consent-accept-button',
                        '.gdpr-consent-accept',
                        '#gdpr-accept',
                        '#consent_prompt_submit',
                        'button.agree-button',
                        'button.btn-accept-all',
                        'button.accept-all-btn',
                        'button.btn-agree',
                        'a.cc-btn.cc-allow',
                        'button[id*="accept"][id*="cookie"]',
                        'button[id*="cookie"][id*="accept"]',
                        'button[id*="accept"][id*="consent"]',
                        'button[id*="consent"][id*="accept"]',
                        'button[class*="cookie"][class*="accept"]',
                        'button[class*="consent"][class*="accept"]',
                        'button[aria-label*="accept all" i]',
                        'button[aria-label*="allow all" i]',
                        'button[aria-label*="accept cookies" i]',
                        'button[aria-label*="allow cookies" i]',
                        'button[aria-label*="agree" i]',
                        'button[id*="accept-all" i]',
                        'button[class*="accept-all" i]',
                        'button[id*="allow-all" i]',
                        'button[class*="allow-all" i]',
                        'button[id="acceptAll"]',
                        'button[id="accept-all"]',
                        'button[id="accept"]',
                        'button[id="agree"]',
                        'button[id="allow"]'
                    ];

                    var acceptTextRegex = /^\s*(accept all cookies|accept all|allow all cookies|allow all|i accept|accept|agree and proceed|agree & close|agree & continue|agree|got it|allow cookies|allow|i agree|ok, got it|ok|understand & accept|akzeptieren|alle akzeptieren|cookies akzeptieren|zustimmen|verstanden|tout accepter|accepter tout|accepter les cookies|accepter|j'accepte|aceptar todas las cookies|aceptar todas|aceptar todo|aceptar cookies|aceptar|de acuerdo|accetta tutti i cookie|accetta tutti|accetta cookie|accetto|accetta|aceitar todos|aceitar cookies|aceitar|concordo|alles accepteren|accepteren|akkoord|akceptuj wszystkie|zaakceptuj wszystkie|akceptuję|zgadzam się|принять все|согласен|соглашаюсь|모두 동의|모두 수락|すべて同意する|すべて許可|同意して進む|同意|接受所有|全部接受|同意并继续)\s*${'$'}/i;

                    function simulateClick(el) {
                        if (!el) return false;
                        try {
                            el.click();
                            return true;
                        } catch(e) {
                            try {
                                var ev = new MouseEvent('click', { bubbles: true, cancelable: true, view: window });
                                el.dispatchEvent(ev);
                                return true;
                            } catch(e2) {
                                return false;
                            }
                        }
                    }

                    function isVisible(el) {
                        if (!el) return false;
                        var style = window.getComputedStyle(el);
                        if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') return false;
                        var rect = el.getBoundingClientRect();
                        return rect.width > 0 && rect.height > 0;
                    }

                    function findAndAcceptCookies() {
                        triggerCmpApis();

                        for (var i = 0; i < knownSelectors.length; i++) {
                            try {
                                var el = document.querySelector(knownSelectors[i]);
                                if (el && isVisible(el)) {
                                    if (simulateClick(el)) {
                                        cleanupOverlays();
                                        return true;
                                    }
                                }
                            } catch(e) {}
                        }

                        var candidates = document.querySelectorAll('button, a, div[role="button"], span[role="button"], input[type="button"], input[type="submit"]');
                        for (var j = 0; j < candidates.length; j++) {
                            var btn = candidates[j];
                            try {
                                if (!isVisible(btn)) continue;
                                var text = (btn.innerText || btn.textContent || btn.value || '').trim();
                                if (!text) continue;

                                if (acceptTextRegex.test(text)) {
                                    var parent = btn.closest('[id*="cookie" i], [class*="cookie" i], [id*="consent" i], [class*="consent" i], [id*="gdpr" i], [class*="gdpr" i], [id*="notice" i], [class*="notice" i], [id*="banner" i], [class*="banner" i], [role="dialog"], [role="alertdialog"], aside, footer, header') || btn.parentElement;
                                    if (parent) {
                                        if (simulateClick(btn)) {
                                            cleanupOverlays();
                                            return true;
                                        }
                                    }
                                }
                            } catch(e) {}
                        }
                        return false;
                    }

                    function cleanupOverlays() {
                        try {
                            if (document.body && document.body.style.overflow === 'hidden') {
                                document.body.style.overflow = '';
                            }
                            if (document.documentElement && document.documentElement.style.overflow === 'hidden') {
                                document.documentElement.style.overflow = '';
                            }
                        } catch(e) {}
                    }

                    findAndAcceptCookies();

                    var delays = [300, 800, 1500, 2500, 4000];
                    delays.forEach(function(delay) {
                        setTimeout(findAndAcceptCookies, delay);
                    });

                    var observer = new MutationObserver(function() {
                        findAndAcceptCookies();
                    });
                    if (document.body || document.documentElement) {
                        observer.observe(document.body || document.documentElement, { childList: true, subtree: true });
                        setTimeout(function() {
                            try { observer.disconnect(); } catch(e) {}
                        }, 10000);
                    }
                } catch(e) {}
            })();
            """.trimIndent(), null
        )
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
                            ins.adsbygoogle, .adsbygoogle, iframe[src*="doubleclick"], iframe[src*="googlesyndication"],
                            iframe[src*="taboola"], iframe[src*="outbrain"], iframe[src*="adsterra"],
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
                            'ins.adsbygoogle', '.adsbygoogle', 'iframe[src*="doubleclick"]', 'iframe[src*="googlesyndication"]',
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

    private fun injectWebFooterHider(webView: WebView?) {
        webView?.evaluateJavascript(
            """
            (function() {
                try {
                    var hideInfoFooters = function() {
                        var selectors = [
                            'footer', '#footer', '[id*="footer"]', '[id*="Footer"]',
                            '.site-footer', '.page-footer', '.main-footer', '.app-footer', '.global-footer', '.footer',
                            '[class*="footer"]', '[class*="Footer"]', 'div[role="contentinfo"]', 'section[role="contentinfo"]',
                            'div[class*="copyright"]', 'div[class*="site-info"]', 'div[class*="legal"]', 'div[class*="policy"]',
                            'section[class*="copyright"]', 'section[class*="legal"]', 'section[class*="policy"]',
                            'div[data-component*="footer"]', 'div[data-test-id*="footer"]'
                        ];
                        
                        var keywords = [
                            '©', 'copyright', 'all rights reserved', 'rights reserved', 'trademarks', 'all rights', 'creative commons',
                            'terms', 'privacy', 'cookie', 'cookies', 'security', 'status', 'legal', 'disclaimer', 'imprint', 'impressum',
                            'privacy policy', 'terms of service', 'terms of use', 'terms & conditions', 'site policy', 'code of conduct',
                            'content policy', 'user agreement', 'manage cookies', 'cookie preferences', 'cookie choices',
                            'do not share my personal', 'do not sell', 'privacy notice', 'refund policy', 'shipping policy',
                            'powered by', 'built with', 'proudly powered by', 'published with', 'sitemap', 'site map',
                            'contact us', 'about us', 'help center', 'documentation', 'editorial guidelines', 'ad choices',
                            'system status', 'footer navigation', 'trust center', 'compliance', 'interest-based ads'
                        ];
                        
                        var footerCandidates = document.querySelectorAll(selectors.join(', '));
                        footerCandidates.forEach(function(el) {
                            var text = (el.innerText || el.textContent || '').toLowerCase();
                            var hasInfoKeyword = keywords.some(function(kw) { return text.includes(kw); });
                            
                            // Protection safeguard: Do NOT hide if element contains tab bars, chat inputs, or app controls
                            var isAppNav = el.querySelector('[role="tablist"], [role="tab"], input, textarea, form, [aria-label*="navigation" i], audio, video, [class*="tab-bar" i], [class*="tabbar" i], [class*="nav-bar" i]');
                            
                            if (hasInfoKeyword && !isAppNav) {
                                el.style.setProperty('display', 'none', 'important');
                                el.style.setProperty('height', '0px', 'important');
                                el.style.setProperty('min-height', '0px', 'important');
                                el.style.setProperty('max-height', '0px', 'important');
                                el.style.setProperty('margin', '0px', 'important');
                                el.style.setProperty('padding', '0px', 'important');
                                el.style.setProperty('visibility', 'hidden', 'important');
                                el.style.setProperty('opacity', '0', 'important');
                                el.style.setProperty('overflow', 'hidden', 'important');
                            }
                        });
                    };
                    hideInfoFooters();
                    var observer = new MutationObserver(hideInfoFooters);
                    observer.observe(document.body || document.documentElement, { childList: true, subtree: true });
                    setInterval(hideInfoFooters, 1500);
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
