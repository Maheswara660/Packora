package com.maheswara660.packora.template

import android.accounts.AccountManager
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
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.RenderProcessGoneDetail
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
import android.content.ClipboardManager
import androidx.appcompat.app.AlertDialog
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.credentials.CustomCredential
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
    private var forceDarkMode: Boolean = false
    private var enableZoom: Boolean = false
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

    private var isShowingError: Boolean = false
    private var lastCheckedMagicLink: String? = null
    private var activeCustomTabAuth: Boolean = false

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) {
            try { binding.webView.onResume() } catch (e: Exception) {}
        }
        checkClipboardForMagicLoginLink()
    }

    private fun checkClipboardForMagicLoginLink() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
            if (!clipboard.hasPrimaryClip()) return
            val item = clipboard.primaryClip?.getItemAt(0) ?: return
            val text = item.text?.toString()?.trim() ?: return

            if (!text.startsWith("http://") && !text.startsWith("https://")) return
            if (text == lastCheckedMagicLink) return

            val targetUrl = config?.optString("targetUrl", "") ?: ""
            val targetHost = try { Uri.parse(targetUrl).host?.lowercase() } catch (e: Exception) { null }
            val uri = try { Uri.parse(text) } catch (e: Exception) { null } ?: return
            val host = uri.host?.lowercase() ?: return

            val isTargetDomain = targetHost != null && (host.endsWith(targetHost) || targetHost.endsWith(host))
            val hasMagicKeywords = text.contains("token=") || text.contains("magic=") || text.contains("auth=") ||
                    text.contains("signin=") || text.contains("callback=") || text.contains("verification=") ||
                    text.contains("session=") || text.contains("code=") || text.contains("login_token=") ||
                    text.contains("email_link=") || text.contains("login?") || text.contains("/verify")

            if (isTargetDomain || (hasMagicKeywords && !isAdOrGamblingUrl(text))) {
                lastCheckedMagicLink = text
                promptOpenMagicLoginLink(text)
            }
        } catch (e: Exception) {}
    }

    private fun promptOpenMagicLoginLink(url: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Email Login Link Detected")
                .setMessage("A login link from your email was copied to your clipboard. Would you like to sign in with it inside this app now?")
                .setPositiveButton("SIGN IN NOW") { _, _ ->
                    binding.webView.loadUrl(url)
                }
                .setNegativeButton("DISMISS", null)
                .show()
        }
    }

    fun showPasteMagicLinkDialog() {
        val input = android.widget.EditText(this).apply {
            hint = "https://... magic login link from email"
            maxLines = 3
            setPadding(48, 32, 48, 32)
            try {
                val clip = (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
                    ?.primaryClip?.getItemAt(0)?.text?.toString()?.trim()
                if (clip != null && (clip.startsWith("http://") || clip.startsWith("https://"))) {
                    setText(clip)
                    setSelection(clip.length)
                }
            } catch (e: Exception) {}
        }

        AlertDialog.Builder(this)
            .setTitle("Sign In with Email Link")
            .setMessage("If this website sent a login or magic link to your email, paste it below to log in directly inside this app:")
            .setView(input)
            .setPositiveButton("SIGN IN") { _, _ ->
                val pasted = input.text.toString().trim()
                if (pasted.startsWith("http://") || pasted.startsWith("https://")) {
                    binding.webView.loadUrl(pasted)
                } else {
                    Toast.makeText(this, "Please paste a valid link starting with http:// or https://", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    fun launchGoogleSignIn(authUrl: String, serverClientId: String? = null) {
        val clientId = serverClientId?.takeIf { it.isNotBlank() } ?: try {
            Uri.parse(authUrl).getQueryParameter("client_id")
        } catch (e: Exception) { null }

        if (!clientId.isNullOrBlank() && clientId.contains("googleusercontent.com")) {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(clientId)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(this@MainActivity)
                credentialManager.getCredentialAsync(
                    context = this@MainActivity,
                    request = request,
                    cancellationSignal = null,
                    executor = ContextCompat.getMainExecutor(this@MainActivity),
                    callback = object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                        override fun onResult(result: GetCredentialResponse) {
                            val cred = result.credential
                            if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                try {
                                    val googleIdToken = GoogleIdTokenCredential.createFrom(cred.data)
                                    val idToken = googleIdToken.idToken
                                    notifyWebPageGoogleIdToken(idToken)
                                    return
                                } catch (e: Exception) {}
                            }
                            launchGoogleOAuthInCustomTab(authUrl)
                        }

                        override fun onError(e: GetCredentialException) {
                            launchGoogleOAuthInCustomTab(authUrl)
                        }
                    }
                )
                return
            } catch (e: Exception) {
                // Fallback to Custom Tab
            }
        }

        launchGoogleOAuthInCustomTab(authUrl)
    }

    private fun launchGoogleOAuthInCustomTab(authUrl: String) {
        try {
            activeCustomTabAuth = true
            val uri = Uri.parse(authUrl)
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            customTabsIntent.launchUrl(this@MainActivity, uri)
        } catch (e: Exception) {
            activeCustomTabAuth = false
            binding.webView.settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
            binding.webView.loadUrl(authUrl)
        }
    }

    private fun notifyWebPageGoogleIdToken(idToken: String) {
        val js = """
            (function() {
                try {
                    if (window.handleCredentialResponse) {
                        window.handleCredentialResponse({ credential: '$idToken' });
                    }
                    if (window.google && window.google.accounts && window.google.accounts.id) {
                        if (window.__gsi_callback) window.__gsi_callback({ credential: '$idToken' });
                    }
                    window.dispatchEvent(new CustomEvent('google-id-token', { detail: { token: '$idToken' } }));
                } catch(e) {}
            })();
        """.trimIndent()
        runOnUiThread {
            binding.webView.evaluateJavascript(js, null)
        }
    }

    // JavaScript Bridge for Google Sign-In button detection and Credential Manager
    inner class GoogleAuthBridge {
        private var detectedClientId: String? = null

        @JavascriptInterface
        fun registerClientId(clientId: String?) {
            if (!clientId.isNullOrBlank()) {
                detectedClientId = clientId
            }
        }

        @JavascriptInterface
        fun triggerGoogleSignIn(authUrl: String? = null, clientId: String? = null) {
            runOnUiThread {
                val finalClientId = clientId?.takeIf { it.isNotBlank() } ?: detectedClientId
                if (!authUrl.isNullOrBlank() && authUrl.contains("accounts.google.com")) {
                    launchGoogleSignIn(authUrl, finalClientId)
                } else if (!finalClientId.isNullOrBlank()) {
                    launchGoogleSignIn(binding.webView.url ?: "", finalClientId)
                }
            }
        }


        @JavascriptInterface
        fun openMagicLinkDialog() {
            runOnUiThread {
                showPasteMagicLinkDialog()
            }
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

        if (savedInstanceState != null) {
            try { binding.webView.restoreState(savedInstanceState) } catch (e: Exception) {}
        }

        binding.btnRetry.setOnClickListener {
            if (!isNetworkAvailable()) {
                binding.errorStatusChip.text = "NO INTERNET DETECTED"
                Toast.makeText(this@MainActivity, "No internet connection detected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Keep error overlay visible while loading; display connecting state
            binding.btnRetry.isEnabled = false
            binding.btnRetryText.text = "CONNECTING..."
            binding.errorStatusChip.text = "CONNECTING..."

            val targetUrl = config?.optString("targetUrl", "")?.takeIf { it.isNotBlank() }
            if (targetUrl != null) {
                binding.webView.loadUrl(targetUrl)
            } else {
                binding.webView.reload()
            }

            // Re-enable button after timeout if still showing error
            binding.btnRetry.postDelayed({
                if (isShowingError) {
                    binding.btnRetry.isEnabled = true
                    binding.btnRetryText.text = "RETRY CONNECTION"
                    val host = try { Uri.parse(targetUrl ?: "").host } catch (e: Exception) { null }
                    binding.errorStatusChip.text = if (!host.isNullOrBlank()) host.uppercase() else "NETWORK OFFLINE"
                }
            }, 6000)
        }

        binding.btnSettings.setOnClickListener {
            try {
                val intent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                try {
                    val intent = Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(intent)
                } catch (ex: Exception) {}
            }
        }

        binding.btnMagicLink.setOnClickListener {
            showPasteMagicLinkDialog()
        }

        val deepLinkUrl = intent.data?.toString()?.takeIf { it.isNotBlank() }
        val targetUrl = deepLinkUrl ?: config?.optString("targetUrl", "")?.takeIf { it.isNotBlank() }

        if (binding.webView.url.isNullOrBlank()) {
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
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val info = cm.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            info.isConnected
        }
    }

    override fun onPause() {
        super.onPause()
        if (::binding.isInitialized) {
            try { binding.webView.onPause() } catch (e: Exception) {}
        }
        try { CookieManager.getInstance().flush() } catch (e: Exception) {}
    }

    override fun onStop() {
        super.onStop()
        try { CookieManager.getInstance().flush() } catch (e: Exception) {}
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::binding.isInitialized) {
            try { binding.webView.saveState(outState) } catch (e: Exception) {}
        }
    }

    override fun onDestroy() {
        if (::binding.isInitialized) {
            try {
                binding.webView.apply {
                    stopLoading()
                    loadUrl("about:blank")
                    clearHistory()
                    removeAllViews()
                    destroy()
                }
            } catch (e: Exception) {}
        }
        super.onDestroy()
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
            isShowingError = true
            binding.btnRetry.isEnabled = true
            binding.btnRetryText.text = "RETRY CONNECTION"

            val isDark = isNightMode
            val overlayBg = if (isDark) Color.parseColor("#0F172A") else Color.parseColor("#F8FAFC")
            val cardBg = if (isDark) Color.parseColor("#1E293B") else Color.parseColor("#FFFFFF")
            val titleColor = if (isDark) Color.parseColor("#F8FAFC") else Color.parseColor("#0F172A")
            val subtextColor = if (isDark) Color.parseColor("#94A3B8") else Color.parseColor("#64748B")

            binding.errorOverlay.setBackgroundColor(overlayBg)
            binding.errorCard.setCardBackgroundColor(cardBg)
            binding.errorTitle.setTextColor(titleColor)
            binding.errorMessage.setTextColor(subtextColor)
            binding.errorStatusChip.setTextColor(subtextColor)
            binding.settingsIcon.setColorFilter(subtextColor)
            binding.btnSettingsText.setTextColor(subtextColor)
            binding.magicLinkIcon.setColorFilter(subtextColor)
            binding.btnMagicLinkText.setTextColor(subtextColor)

            val targetUrl = config?.optString("targetUrl", "") ?: ""
            val host = try { Uri.parse(targetUrl).host } catch (e: Exception) { null }
            binding.errorStatusChip.text = if (!host.isNullOrBlank()) host.uppercase() else "NETWORK OFFLINE"

            binding.errorOverlay.visibility = View.VISIBLE
            binding.webView.visibility = View.GONE
        }
    }

    private fun hideErrorOverlay() {
        runOnUiThread {
            isShowingError = false
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
                val shouldDarken = forceDarkMode || isNightMode
                if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, shouldDarken)
                } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    @Suppress("DEPRECATION")
                    WebSettingsCompat.setForceDark(
                        settings,
                        if (shouldDarken) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
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
        forceDarkMode = webViewConfig?.optBoolean("forceDarkMode", false) ?: false
        enableZoom = webViewConfig?.optBoolean("enableZoom", false) ?: false
        hideWebFooter = if (webViewConfig?.has("hideWebFooter") == true) {
            webViewConfig.optBoolean("hideWebFooter", true)
        } else {
            !(webViewConfig?.optBoolean("enableWebFooter", false) ?: false)
        }

        settings.setSupportZoom(enableZoom)
        settings.builtInZoomControls = enableZoom
        settings.displayZoomControls = false

        // Force Dark / Algorithmic Darkening
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val shouldDarken = forceDarkMode || isNightMode
                if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, shouldDarken)
                } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    @Suppress("DEPRECATION")
                    WebSettingsCompat.setForceDark(
                        settings,
                        if (shouldDarken) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
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
        webView.addJavascriptInterface(GoogleAuthBridge(), "AndroidGoogleAuth")

        if (isDesktopMode) {
            val desktopUA = webViewConfig?.optString("userAgent")
                ?.takeIf { it.isNotBlank() }
                ?: "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

            settings.userAgentString = desktopUA
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.defaultTextEncodingName = "utf-8"
        } else {
            // Clean standard Chrome User Agent so Google OAuth and auth flows work seamlessly without 403
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
            settings.useWideViewPort = false
            settings.loadWithOverviewMode = false
        }

        // Strip X-Requested-With header to prevent Google and OAuth providers from returning 403 disallowed_useragent
        try {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST)) {
                WebSettingsCompat.setRequestedWithHeaderOriginAllowList(settings, emptySet())
            }
        } catch (e: Exception) {}

        webView.webViewClient = object : WebViewClient() {

            // AdBlocker Request Interception
            override fun shouldInterceptRequest(
                view: WebView?, request: WebResourceRequest?
            ): WebResourceResponse? {
                if (request == null) return null

                // 1. Never block the main website frame
                if (request.isForMainFrame) {
                    return null
                }

                val requestUrl = request.url?.toString() ?: return null
                val lowerUrl = requestUrl.lowercase()

                // 2. Safety Whitelist: Never block media streams, video segments, audio, fonts, or blob/data
                if (lowerUrl.endsWith(".m3u8") || lowerUrl.endsWith(".mp4") || lowerUrl.endsWith(".webm") ||
                    lowerUrl.endsWith(".ts") || lowerUrl.endsWith(".mp3") || lowerUrl.endsWith(".m4s") ||
                    lowerUrl.endsWith(".mpd") || lowerUrl.startsWith("blob:") || lowerUrl.startsWith("data:")
                ) {
                    return null
                }
                val acceptHeader = request.requestHeaders?.get("Accept")?.lowercase() ?: ""
                if (acceptHeader.contains("video/") || acceptHeader.contains("audio/")) {
                    return null
                }

                // 3. Block verified ad & tracking network domains
                if (isAdOrGamblingUrl(requestUrl)) {
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
                // Note: Do not hide error overlay here; error overlay is hidden in onPageFinished once page loads successfully
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

                if (hideWebFooter) {
                    injectWebFooterHider(view)
                }

                if (enableZoom) {
                    injectZoomViewportOverride(view)
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

                // Hide error screen only when page has finished loading successfully with a valid URL
                if (isShowingError && url != null && url != "about:blank" && !url.startsWith("data:")) {
                    hideErrorOverlay()
                }

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

                if (enableZoom) {
                    injectZoomViewportOverride(view)
                }

                // Inject Google SSO button click interceptor & GIS client_id detector
                view?.evaluateJavascript(
                    """
                    (function() {
                        try {
                            var attachGoogleListener = function() {
                                var gisEl = document.querySelector('#g_id_onload, [data-client_id], meta[name="google-signin-client_id"]');
                                var cid = gisEl ? (gisEl.getAttribute('data-client_id') || gisEl.getAttribute('content')) : null;
                                if (cid && window.AndroidGoogleAuth && window.AndroidGoogleAuth.registerClientId) {
                                    window.AndroidGoogleAuth.registerClientId(cid);
                                }

                                var googleButtons = document.querySelectorAll('[data-provider="google"], [id*="google" i], [class*="google" i], a[href*="accounts.google.com"], button[aria-label*="google" i]');
                                googleButtons.forEach(function(btn) {
                                    if (btn.__packora_google_attached) return;
                                    btn.__packora_google_attached = true;
                                    btn.addEventListener('click', function(e) {
                                        var href = (btn.tagName === 'A' && btn.href) ? btn.href : null;
                                        if (cid && window.AndroidGoogleAuth && window.AndroidGoogleAuth.registerClientId) {
                                            window.AndroidGoogleAuth.registerClientId(cid);
                                        }
                                    }, false);
                                });
                            };
                            attachGoogleListener();
                            new MutationObserver(attachGoogleListener).observe(document.body || document.documentElement, { childList: true, subtree: true });
                        } catch(e) {}
                    })();
                    """.trimIndent(), null
                )

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

            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                view?.let { wv ->
                    val container = wv.parent as? ViewGroup
                    container?.removeView(wv)
                    wv.destroy()
                }
                recreate()
                return true
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

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                val code = errorResponse?.statusCode ?: 0
                if (request?.isForMainFrame == true && (code in 500..599 || code == 404)) {
                    showErrorOverlay()
                }
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url == null) return false
                val uri = try { Uri.parse(url) } catch (e: Exception) { return false }
                return handleUrlNavigation(view, url, uri, isMainFrame = true, isRedirect = false)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                val uri = request.url ?: Uri.parse(url)
                return handleUrlNavigation(view, url, uri, request.isForMainFrame, request.isRedirect)
            }

            private fun handleUrlNavigation(
                view: WebView?, url: String, uri: Uri, isMainFrame: Boolean, isRedirect: Boolean
            ): Boolean {
                // 1. Block ad, popunder, and gambling promotional redirect links
                if (isAdOrGamblingUrl(url)) {
                    return true
                }

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

                val targetUrl = config?.optString("targetUrl", "") ?: ""
                val targetHost = try { Uri.parse(targetUrl).host?.lowercase() } catch (e: Exception) { null }
                val currentHost = uri.host?.lowercase()

                val isSameDomainFamily = targetHost != null && currentHost != null && (
                    currentHost.endsWith(targetHost) || targetHost.endsWith(currentHost) ||
                    currentHost.removePrefix("www.") == targetHost.removePrefix("www.") ||
                    currentHost.split(".").takeLast(2) == targetHost.split(".").takeLast(2)
                )

                // 2. Intra-domain navigation and auth flows MUST remain directly inside this app's WebView
                if (isSameDomainFamily || url.contains("accounts.google.com") || isAuthOrLoginUrl(url, uri.host)) {
                    view?.settings?.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                    return false // Load internally in app WebView!
                }

                // 3. Deep Linking to Popular Installed Native Apps (YouTube, Maps, etc.) - never other Packora apps
                if (tryLaunchInInstalledNativeApp(url)) {
                    return true
                }

                val openExternalLinks = webViewConfig?.optBoolean("openExternalLinks", false) ?: false

                if (openExternalLinks && targetUrl.isNotEmpty()) {
                    // Only open external non-auth links in Custom Tabs
                    if (!isSameDomainFamily && !isAuthOrLoginUrl(url, uri.host) && isMainFrame && !isRedirect) {
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

                // Check if the popup target belongs to the current app's domain
                val targetUrl = config?.optString("targetUrl", "") ?: ""
                val targetHost = try { Uri.parse(targetUrl).host?.lowercase() } catch (e: Exception) { null }
                val popupHost = try { if (!popupUrl.isNullOrBlank()) Uri.parse(popupUrl).host?.lowercase() else null } catch (e: Exception) { null }

                if (targetHost != null && popupHost != null) {
                    val cleanTarget = targetHost.removePrefix("www.")
                    val cleanPopup = popupHost.removePrefix("www.")
                    if (cleanPopup == cleanTarget || cleanPopup.endsWith(".$cleanTarget") || cleanTarget.endsWith(".$cleanPopup")) {
                        // Same domain link with target="_blank" -> Load directly in main WebView, no popup dialog needed!
                        transport.webView = view
                        resultMsg.sendToTarget()
                        return true
                    }
                }

                val isDark = isNightMode
                val popupBgColor = if (isDark) Color.parseColor("#121212") else Color.WHITE
                val topBarBgColor = if (isDark) Color.parseColor("#1E293B") else Color.parseColor("#F1F5F9")
                val textColor = if (isDark) Color.parseColor("#F8FAFC") else Color.parseColor("#0F172A")
                val subtextColor = if (isDark) Color.parseColor("#94A3B8") else Color.parseColor("#64748B")

                val popupWebView = WebView(this@MainActivity).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    @Suppress("DEPRECATION")
                    settings.databaseEnabled = true
                    settings.setSupportMultipleWindows(true)
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    setBackgroundColor(popupBgColor)

                    // Strip X-Requested-With header on popup WebView so Google doesn't block with 403 disallowed_useragent
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            if (WebViewFeature.isFeatureSupported(WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST)) {
                                WebSettingsCompat.setRequestedWithHeaderOriginAllowList(settings, emptySet())
                            }
                        } catch (e: Exception) {}
                    }
                }

                val container = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    setBackgroundColor(popupBgColor)
                }

                val topBar = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (52 * resources.displayMetrics.density).toInt())
                    gravity = Gravity.CENTER_VERTICAL
                    setBackgroundColor(topBarBgColor)
                    setPadding((16 * resources.displayMetrics.density).toInt(), 0, (12 * resources.displayMetrics.density).toInt(), 0)
                }

                val titleView = TextView(this@MainActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    text = "Sign In / Secure Window"
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(textColor)
                }

                val closeBtn = ImageView(this@MainActivity).apply {
                    val size = (36 * resources.displayMetrics.density).toInt()
                    layoutParams = LinearLayout.LayoutParams(size, size)
                    setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                    setColorFilter(subtextColor)
                    setPadding((6 * resources.displayMetrics.density).toInt(), (6 * resources.displayMetrics.density).toInt(), (6 * resources.displayMetrics.density).toInt(), (6 * resources.displayMetrics.density).toInt())
                    contentDescription = "Close"
                }

                val progressBar = ProgressBar(this@MainActivity, null, android.R.attr.progressBarStyleHorizontal).apply {
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (3 * resources.displayMetrics.density).toInt())
                    max = 100
                    progress = 10
                }

                topBar.addView(titleView)
                topBar.addView(closeBtn)
                container.addView(topBar)
                container.addView(progressBar)

                popupWebView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
                container.addView(popupWebView)

                val popupDialog = Dialog(this@MainActivity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen).apply {
                    setContentView(container)
                    window?.apply {
                        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    }
                    setCanceledOnTouchOutside(true)
                    setCancelable(true)
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

                closeBtn.setOnClickListener {
                    try { popupDialog.dismiss() } catch (e: Exception) {}
                }

                popupWebView.webChromeClient = object : WebChromeClient() {
                    override fun onCloseWindow(window: WebView?) {
                        try { popupDialog.dismiss() } catch (e: Exception) {}
                    }

                    override fun onProgressChanged(v: WebView?, newProgress: Int) {
                        super.onProgressChanged(v, newProgress)
                        progressBar.progress = newProgress
                        progressBar.visibility = if (newProgress >= 100) View.GONE else View.VISIBLE
                    }

                    override fun onReceivedTitle(v: WebView?, title: String?) {
                        super.onReceivedTitle(v, title)
                        if (!title.isNullOrBlank()) {
                            titleView.text = title
                        }
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

                        // If login flow completed in popup and navigated back to target app domain, auto-close popup & refresh main WebView
                        val currentHost = try { Uri.parse(url ?: "").host?.lowercase() } catch (e: Exception) { null }
                        if (targetHost != null && currentHost != null && (currentHost.endsWith(targetHost) || targetHost.endsWith(currentHost)) && !isAuthOrLoginUrl(url)) {
                            try {
                                popupDialog.dismiss()
                                binding.webView.reload()
                            } catch (e: Exception) {}
                        }
                    }

                    @Suppress("DEPRECATION")
                    override fun shouldOverrideUrlLoading(v: WebView?, url: String?): Boolean {
                        if (url == null) return false
                        if (isAdOrGamblingUrl(url)) return true
                        if (url.contains("accounts.google.com") || isAuthOrLoginUrl(url)) {
                            v?.settings?.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                            return false
                        }
                        // Check if returned to main app
                        val currentHost = try { Uri.parse(url).host?.lowercase() } catch (e: Exception) { null }
                        if (targetHost != null && currentHost != null && (currentHost.endsWith(targetHost) || targetHost.endsWith(currentHost)) && !isAuthOrLoginUrl(url)) {
                            try {
                                popupDialog.dismiss()
                                binding.webView.loadUrl(url)
                                return true
                            } catch (e: Exception) {}
                        }
                        return false
                    }

                    override fun shouldOverrideUrlLoading(v: WebView?, req: WebResourceRequest?): Boolean {
                        val target = req?.url?.toString() ?: return false
                        if (isAdOrGamblingUrl(target)) {
                            return true
                        }
                        if (target.contains("accounts.google.com") || isAuthOrLoginUrl(target)) {
                            v?.settings?.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                            return false // Let Google Account Chooser & Auth load inside popup window!
                        }
                        // Check if returned to main app
                        val currentHost = try { req.url?.host?.lowercase() } catch (e: Exception) { null }
                        if (targetHost != null && currentHost != null && (currentHost.endsWith(targetHost) || targetHost.endsWith(currentHost)) && !isAuthOrLoginUrl(target)) {
                            try {
                                popupDialog.dismiss()
                                binding.webView.loadUrl(target)
                                return true
                            } catch (e: Exception) {}
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
            if (isAuthOrLoginUrl(url)) {
                return false // Protect auth and login links from being intercepted by external apps/browsers
            }
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

            // Check if ANY installed external native app handles this link/domain
            if (scheme == "http" || scheme == "https") {
                // NEVER delegate intra-domain links or login/auth links externally
                val targetUrl = config?.optString("targetUrl", "") ?: ""
                val targetHost = try { Uri.parse(targetUrl).host?.lowercase() } catch (e: Exception) { null }
                val currentHost = uri.host?.lowercase()

                if (targetHost != null && currentHost != null) {
                    val cleanTarget = targetHost.removePrefix("www.")
                    val cleanCurrent = currentHost.removePrefix("www.")
                    if (cleanCurrent == cleanTarget ||
                        cleanCurrent.endsWith(".$cleanTarget") ||
                        cleanTarget.endsWith(".$cleanCurrent") ||
                        cleanCurrent.split(".").takeLast(2) == cleanTarget.split(".").takeLast(2)
                    ) {
                        return false // Must stay inside current app's WebView!
                    }
                }

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
                    "com.android.browser", "com.google.android.apps.chrome", packageName
                )

                // Strictly match real external native apps (YouTube, Maps, etc.) and NEVER match other Packora-compiled apps
                val matchingApp = resolveInfoList.firstOrNull { info ->
                    val pkg = info.activityInfo.packageName
                    val activityName = info.activityInfo.name
                    pkg != packageName &&
                        pkg !in browserPackages &&
                        !pkg.contains("browser") &&
                        !pkg.startsWith("com.maheswara660.packora") &&
                        !pkg.contains("packora") &&
                        activityName != "com.maheswara660.packora.template.MainActivity" &&
                        !activityName.endsWith(".MainActivity")
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
                            .pubnation-ad, .adbox, #adbox, .ad_box, #ad_box {
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
                            '.ad-container', '.ad-wrapper', '.ad-slot', '.ad-unit', '.sponsored-content', '.pubnation-ad'
                        ];
                        selectors.forEach(function(s) {
                            try {
                                document.querySelectorAll(s).forEach(function(el) {
                                    // Safeguard: Never collapse or disable elements containing media or form controls
                                    if (el.querySelector('video, audio, input, textarea, button, [role="button"]')) return;
                                    el.style.setProperty('display', 'none', 'important');
                                    el.style.setProperty('height', '0px', 'important');
                                    el.style.setProperty('pointer-events', 'none', 'important');
                                });
                            } catch(e) {}
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
                    // 1. Instant CSS style injection
                    if (!document.getElementById('__packora_footer_style')) {
                        var style = document.createElement('style');
                        style.id = '__packora_footer_style';
                        style.textContent = `
                            footer, [role="contentinfo"], .site-footer, .main-footer, .page-footer, .global-footer, .app-footer,
                            #footer, #colophon, .colophon, .footer-container, .footer-content, .footer-wrapper, .footer-bottom,
                            div[class*="site-footer" i], div[class*="main-footer" i], div[class*="page-footer" i],
                            div[class*="global-footer" i], div[class*="copyright" i], p[class*="copyright" i],
                            section[class*="site-footer" i], section[class*="main-footer" i] {
                                display: none !important;
                                visibility: hidden !important;
                                height: 0px !important;
                                min-height: 0px !important;
                                max-height: 0px !important;
                                margin: 0px !important;
                                padding: 0px !important;
                                opacity: 0 !important;
                                overflow: hidden !important;
                                pointer-events: none !important;
                            }
                        `;
                        (document.head || document.documentElement).appendChild(style);
                    }

                    // 2. Intelligent DOM cleaner
                    function queryAll(selector, root) {
                        root = root || document;
                        var list = [];
                        try {
                            list = Array.prototype.slice.call(root.querySelectorAll(selector));
                        } catch(e) {}
                        try {
                            var all = root.querySelectorAll('*');
                            for (var i = 0; i < all.length; i++) {
                                if (all[i].shadowRoot) {
                                    list = list.concat(queryAll(selector, all[i].shadowRoot));
                                }
                            }
                        } catch(e) {}
                        return list;
                    }

                    var isBottomDocked = function(el) {
                        try {
                            var style = window.getComputedStyle(el);
                            if (style.position === 'fixed' || style.position === 'sticky') {
                                var rect = el.getBoundingClientRect();
                                if (rect.bottom >= window.innerHeight - 30 && rect.top > 0) {
                                    return true;
                                }
                            }
                        } catch(e) {}
                        return false;
                    };

                    var selectors = [
                        'footer', '#footer', '[id*="footer" i]', '#colophon', '.colophon', '[id*="colophon" i]',
                        '.site-footer', '.page-footer', '.main-footer', '.app-footer', '.global-footer', '.sub-footer', '.footer',
                        '[class*="footer" i]', '[class*="Footer" i]', '[class*="subfooter" i]', '[class*="prefooter" i]',
                        '[class*="fat-footer" i]', '[class*="socket" i]', '[class*="site-bottom" i]', '[class*="attribution" i]',
                        'div[role="contentinfo"]', 'section[role="contentinfo"]', 'aside[role="contentinfo"]',
                        'div[class*="copyright" i]', 'section[class*="copyright" i]', 'p[class*="copyright" i]', 'span[class*="copyright" i]',
                        'div[class*="site-info" i]', 'div[class*="legal" i]', 'section[class*="legal" i]', 'div[class*="policy" i]', 'section[class*="policy" i]',
                        'div[class*="disclaimer" i]', 'section[class*="disclaimer" i]', 'div[id*="disclaimer" i]',
                        'div[data-component*="footer" i]', 'div[data-test-id*="footer" i]', 'div[data-testid*="footer" i]', 'div[data-cy*="footer" i]',
                        '[data-section="footer"]', '[data-area="footer"]', '[data-widget-type="footer"]',
                        '[aria-label*="footer" i]', '.cookie-banner', '.privacy-banner', '.gdpr-banner', '.ccpa-banner', '.consent-banner',
                        '.footer-container', '.footer-wrapper', '.footer-content', '.footer-links', '.footer-nav', '.footer-bottom', '.bottom-footer',
                        '.site-subfooter', '.site_footer'
                    ];
                    
                    var keywords = [
                        '©', 'copyright', 'all rights reserved', 'rights reserved', 'trademarks', 'all rights', 'creative commons',
                        'terms', 'privacy', 'cookie', 'cookies', 'security', 'status', 'legal', 'disclaimer', 'imprint', 'impressum',
                        'privacy policy', 'terms of service', 'terms of use', 'terms & conditions', 'site policy', 'manage cookies',
                        'do not sell', 'your privacy choices', 'ccpa', 'gdpr', 'affiliate disclosure', 'powered by', 'built with',
                        'sitemap', 'site map', 'contact us', 'about us', 'help center', 'footer navigation',
                        'subscribe to our newsletter', 'newsletter signup', 'sign up for newsletter',
                        'haftungsausschluss', 'datenschutz', 'mentions légales', 'politique de confidentialité',
                        'términos y condiciones', 'política de privacidad', 'informativa sulla privacy',
                        'termos de uso', 'algemene voorwaarden', 'användarvillkor', 'regulamin',
                        'все права защищены', '版权所有', '無断転載を禁じます', '모든 권리 보유', 'सर्वाधिकार सुरक्षित'
                    ];

                    var hideInfoFooters = function() {
                        var footerCandidates = queryAll(selectors.join(', '));
                        footerCandidates.forEach(function(el) {
                            var tag = (el.tagName || '').toUpperCase();
                            var role = (el.getAttribute('role') || '').toLowerCase();
                            var id = (el.id || '').toLowerCase();
                            var bottomDocked = isBottomDocked(el);

                            // Only protect genuine app bottom navigation tab bars (e.g. fixed bottom bar with tabs)
                            var isAppTabBar = bottomDocked && el.querySelector('[role="tablist"], [class*="tab-bar" i], [class*="tabbar" i], [class*="bottom-nav" i]');
                            if (isAppTabBar) return;

                            var text = (el.innerText || el.textContent || '').toLowerCase();
                            var hasInfoKeyword = keywords.some(function(kw) { return text.includes(kw); });
                            var isSemanticFooter = (tag === 'FOOTER' || role === 'contentinfo' || id === 'footer');

                            if (isSemanticFooter || hasInfoKeyword || (bottomDocked && keywords.slice(0, 10).some(function(kw) { return text.includes(kw); }))) {
                                el.style.setProperty('display', 'none', 'important');
                                el.style.setProperty('height', '0px', 'important');
                                el.style.setProperty('min-height', '0px', 'important');
                                el.style.setProperty('max-height', '0px', 'important');
                                el.style.setProperty('margin', '0px', 'important');
                                el.style.setProperty('padding', '0px', 'important');
                                el.style.setProperty('visibility', 'hidden', 'important');
                                el.style.setProperty('opacity', '0', 'important');
                                el.style.setProperty('overflow', 'hidden', 'important');
                                el.style.setProperty('pointer-events', 'none', 'important');
                            }
                        });
                    };

                    hideInfoFooters();
                    if (!window.__packora_footer_observer) {
                        window.__packora_footer_observer = new MutationObserver(hideInfoFooters);
                        window.__packora_footer_observer.observe(document.body || document.documentElement, { childList: true, subtree: true });
                        setInterval(hideInfoFooters, 1500);
                    }
                } catch(e) {}
            })();
            """.trimIndent(), null
        )
    }

    private fun injectZoomViewportOverride(webView: WebView?) {
        webView?.evaluateJavascript(
            """
            (function() {
                try {
                    var metas = document.querySelectorAll('meta[name="viewport"]');
                    metas.forEach(function(meta) {
                        var content = meta.getAttribute('content') || '';
                        if (content.includes('user-scalable=no') || content.includes('maximum-scale=1')) {
                            content = content.replace(/user-scalable\s*=\s*no/gi, 'user-scalable=yes')
                                             .replace(/maximum-scale\s*=\s*[0-9.]+/gi, 'maximum-scale=5.0');
                            meta.setAttribute('content', content);
                        }
                    });
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

        val webViewBg = if (bottomColor != 0 && bottomColor != Color.TRANSPARENT) {
            bottomColor
        } else if (isNightMode) {
            Color.parseColor("#121212")
        } else {
            Color.WHITE
        }

        window.decorView.setBackgroundColor(topColor)
        binding.root.setBackgroundColor(topColor)
        binding.webView.setBackgroundColor(webViewBg)
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
        val uri = try { Uri.parse(urlString) } catch (e: Exception) { null }
        val host = uri?.host?.lowercase() ?: ""
        val path = uri?.path?.lowercase() ?: ""

        // Never block streaming media or data blobs
        if (lower.endsWith(".m3u8") || lower.endsWith(".mp4") || lower.endsWith(".webm") ||
            lower.endsWith(".ts") || lower.endsWith(".mp3") || lower.endsWith(".m4s") ||
            lower.endsWith(".mpd") || lower.startsWith("blob:") || lower.startsWith("data:")
        ) {
            return false
        }

        // Verified ad network and tracking domains
        val adDomains = listOf(
            "googlesyndication.com", "doubleclick.net", "adservice.google.",
            "adsterra.com", "propellerads.com", "popcash.net", "popads.net",
            "exoclick.com", "trafficjunky.com", "mgid.com", "revcontent.com",
            "taboola.com", "outbrain.com", "adnxs.com", "criteo.com",
            "rubiconproject.com", "pubmatic.com", "openx.net", "bidswitch.net",
            "smartadserver.com", "adroll.com", "applovin.com", "unityads.unity3d.com",
            "ironsrc.com", "vungle.com", "inmobi.com", "amazon-adsystem.com",
            "zedo.com", "media.net", "scorecardresearch.com", "quantserve.com",
            "advertising.com", "fastclick.net", "revenuehits.com", "onclickads.net",
            "adcolony.com", "chartboost.com", "yieldmo.com", "exponential.com"
        )
        if (adDomains.any { host.endsWith(it) || host.contains(".$it") || host == it }) {
            return true
        }

        // Verified gambling & betting domains
        val gamblingDomains = listOf(
            "bet365.", "1xbet.", "parimatch.", "betway.", "stake.com",
            "dafabet.", "mostbet.", "bet9ja.", "melbet.", "888casino.",
            "betfair.", "bwin.", "williamhill.", "pokerstars.", "draftkings.",
            "fanduel.", "betonline.", "bovada."
        )
        if (gamblingDomains.any { host.contains(it) }) {
            return true
        }

        // Specific ad-delivery paths
        val adPathKeywords = listOf(
            "/adserver/", "/ad-delivery/", "/ad_delivery/", "/popunder",
            "/direct-ad/", "/adclick", "/ad-click", "/click-redirect"
        )
        if (adPathKeywords.any { path.contains(it) }) {
            return true
        }

        return false
    }

    private fun isAuthOrLoginUrl(urlString: String?, hostString: String? = null): Boolean {
        if (urlString.isNullOrBlank()) return false
        val lowerUrl = urlString.lowercase()
        val host = (hostString ?: try { Uri.parse(urlString).host } catch (e: Exception) { null })?.lowercase() ?: ""

        // 1. Identity Providers & Auth Platforms Domains
        val isAuthHost = host.contains("accounts.google") ||
            host.contains("myaccount.google") ||
            host.contains("googleid.google") ||
            host.contains("oauth2.googleapis") ||
            host.contains("login.microsoftonline") ||
            host.contains("login.live.com") ||
            host.contains("account.microsoft") ||
            host.contains("login.windows.net") ||
            host.contains("b2clogin.com") ||
            host.contains("appleid.apple") ||
            host.contains("idmsa.apple") ||
            host.contains("auth.apple") ||
            host.contains("signin.aws") ||
            host.contains("cognito") ||
            host.contains("auth0") ||
            host.contains("okta") ||
            host.contains("onelogin") ||
            host.contains("pingidentity") ||
            host.contains("ping.identity") ||
            host.contains("pingone") ||
            host.contains("sailpoint") ||
            host.contains("forgerock") ||
            host.contains("keycloak") ||
            host.contains("identityserver") ||
            host.contains("shibboleth") ||
            host.contains("clerk.") ||
            host.contains("clerkstage") ||
            host.contains("clerkdev") ||
            host.contains("clerk.com") ||
            host.contains("firebaseapp") ||
            host.contains("supabase.co") ||
            host.contains("workos") ||
            host.contains("stytch") ||
            host.contains("descope") ||
            host.contains("magic.link") ||
            host.contains("kinde.com") ||
            host.contains("passage.id") ||
            host.contains("logto.io") ||
            host.contains("zitadel") ||
            host.contains("oryapis") ||
            host.contains("ory.sh") ||
            host.contains("fusionauth") ||
            host.contains("cotter.app") ||
            host.contains("appwrite.io") ||
            host.contains("nhost.io") ||
            host.contains("duo.com") ||
            host.contains("recaptcha") ||
            host.contains("hcaptcha") ||
            host.contains("turnstile") ||
            host.contains("challenges.cloudflare") ||
            // Subdomain prefixes for auth / identity
            host.startsWith("auth.") ||
            host.startsWith("auth-") ||
            host.startsWith("authentication.") ||
            host.startsWith("login.") ||
            host.startsWith("signin.") ||
            host.startsWith("sign-in.") ||
            host.startsWith("signup.") ||
            host.startsWith("sign-up.") ||
            host.startsWith("register.") ||
            host.startsWith("registration.") ||
            host.startsWith("sso.") ||
            host.startsWith("id.") ||
            host.startsWith("idp.") ||
            host.startsWith("identity.") ||
            host.startsWith("iam.") ||
            host.startsWith("accounts.") ||
            host.startsWith("account.") ||
            host.startsWith("user.") ||
            host.startsWith("users.") ||
            host.startsWith("profile.") ||
            host.startsWith("portal.") ||
            host.startsWith("member.") ||
            host.startsWith("members.") ||
            host.startsWith("client.") ||
            host.startsWith("clients.") ||
            host.startsWith("customer.") ||
            host.startsWith("customers.") ||
            host.startsWith("secure.") ||
            host.startsWith("session.") ||
            host.startsWith("sessions.") ||
            host.startsWith("oauth.") ||
            host.startsWith("openid.") ||
            host.startsWith("oidc.") ||
            host.startsWith("saml.") ||
            host.startsWith("ldap.") ||
            host.startsWith("token.") ||
            host.startsWith("tokens.") ||
            host.startsWith("authorize.") ||
            host.startsWith("connect.") ||
            host.startsWith("verify.") ||
            host.startsWith("2fa.") ||
            host.startsWith("mfa.") ||
            host.startsWith("otp.") ||
            host.startsWith("passkey.") ||
            host.startsWith("passkeys.") ||
            host.startsWith("webauthn.") ||
            // Substring auth indicators in domain
            host.contains(".auth.") ||
            host.contains(".identity.") ||
            host.contains(".iam.") ||
            host.contains(".sso.") ||
            host.contains(".oauth.") ||
            host.contains("federat")

        if (isAuthHost) return true

        // 2. OAuth provider domains with login / oauth subpaths
        if ((host.contains("github.com") && (lowerUrl.contains("/login") || lowerUrl.contains("/session") || lowerUrl.contains("/oauth"))) ||
            (host.contains("gitlab.com") && (lowerUrl.contains("/sign_in") || lowerUrl.contains("/oauth"))) ||
            (host.contains("linkedin.com") && (lowerUrl.contains("/uas/login") || lowerUrl.contains("/oauth") || lowerUrl.contains("/checkpoint"))) ||
            (host.contains("facebook.com") && (lowerUrl.contains("/dialog/oauth") || lowerUrl.contains("/login"))) ||
            ((host.contains("twitter.com") || host.contains("x.com")) && (lowerUrl.contains("/i/flow/login") || lowerUrl.contains("/oauth") || lowerUrl.contains("/login"))) ||
            (host.contains("discord.com") && (lowerUrl.contains("/login") || lowerUrl.contains("/oauth2"))) ||
            (host.contains("slack.com") && (lowerUrl.contains("/oauth") || lowerUrl.contains("/signin") || lowerUrl.contains("/login"))) ||
            (host.contains("amazon.com") && (lowerUrl.contains("/ap/signin") || lowerUrl.contains("/oauth"))) ||
            (host.contains("atlassian.com") && lowerUrl.contains("/login")) ||
            (host.contains("spotify.com") && (lowerUrl.contains("/login") || lowerUrl.contains("/authorize"))) ||
            (host.contains("uber.com") && lowerUrl.contains("/login")) ||
            (host.contains("yahoo.com") && lowerUrl.contains("/login")) ||
            (host.contains("steamcommunity.com") && lowerUrl.contains("/openid")) ||
            (host.contains("twitch.tv") && lowerUrl.contains("/login")) ||
            (host.contains("reddit.com") && lowerUrl.contains("/login")) ||
            (host.contains("paypal.com") && lowerUrl.contains("/signin")) ||
            (host.contains("dropbox.com") && lowerUrl.contains("/login")) ||
            (host.contains("zoom.us") && (lowerUrl.contains("/oauth") || lowerUrl.contains("/signin")))
        ) {
            return true
        }

        // 3. Path and Full URL Keywords (Catching SPAs, query parameters, hash fragments, and multi-lingual routes)
        val uri = try { Uri.parse(urlString) } catch (e: Exception) { null }
        val path = uri?.path?.lowercase() ?: ""

        val authKeywords = listOf(
            // English standard routes
            "login", "log-in", "log_in", "signin", "sign-in", "sign_in",
            "signup", "sign-up", "sign_up", "register", "registration",
            "join", "join-now", "joinus", "enroll", "enrollment",
            "create-account", "create_account", "createaccount",
            "new-account", "new_account", "new-user", "new_user", "newuser",
            "onboarding", "authenticate", "authentication",
            "user-login", "user_login", "member-login", "member_login",
            "client-login", "client_login", "customer-login", "customer_login",
            "account-login", "portal-login", "admin-login", "wp-login",
            // OAuth, SSO & Identity protocols
            "oauth", "oauth2", "openid", "oidc", "authorize", "authorization",
            "sso", "saml", "saml2", "idp", "session", "sessions",
            "token", "access_token", "id_token", "refresh_token",
            "grant_type", "client_id", "response_type", "redirect_uri",
            "code_challenge", "code_verifier", "nonce", "state",
            "continue_with_google", "continue-with", "signin/oauth",
            "oauth/authorize", "select_account", "login_hint",
            "oauth-callback", "auth-callback", "api/auth",
            // Password & Account Recovery
            "password", "forgot-password", "forgot_password", "forgotpassword",
            "reset-password", "reset_password", "resetpassword",
            "change-password", "change_password", "recover", "recovery",
            "magic-link", "magiclink", "magic_link",
            // 2FA, OTP & Passkeys
            "verify", "verification", "verify-email", "confirm-email", "check-email",
            "two-factor", "twofactor", "2fa", "mfa", "totp", "otp",
            "passcode", "passkey", "passkeys", "webauthn", "fido", "fido2",
            "checkpoint", "challenge", "security-check", "captcha",
            // Spanish
            "iniciar-sesion", "iniciar_sesion", "iniciarsesion", "ingresar", "ingreso", "registrarse", "registro", "crear-cuenta",
            // French
            "connexion", "se-connecter", "identification", "inscription", "creer-un-compte",
            // German
            "anmelden", "anmeldung", "einloggen", "registrieren", "registrierung", "konto-erstellen",
            // Portuguese
            "entrar", "acesso", "cadastrar", "cadastro", "criar-conta",
            // Italian
            "accedi", "accesso", "registrati", "registrazione"
        )

        // Match against path segments
        if (authKeywords.any { kw -> path.contains(kw) }) return true

        // Match against entire URL (for SPAs, hash routing e.g. #/login, and query params e.g. ?action=login)
        val spaAndQueryPatterns = listOf(
            "#/login", "#/signin", "#/signup", "#/register", "#/auth", "#!/login", "#login", "#signin", "#signup",
            "action=login", "action=signin", "action=signup", "action=register",
            "mode=login", "mode=signin", "mode=signup",
            "view=login", "view=signin", "view=signup",
            "prompt=login", "prompt=consent", "prompt=select_account", "prompt=none",
            "screen=login", "screen=signup", "type=login", "type=signup",
            "flow=login", "flow=signup", "auth=true",
            "code_challenge=", "state=", "nonce=", "discovery",
            ".well-known/openid-configuration", "oauth_verifier=", "oauth_token="
        )
        if (spaAndQueryPatterns.any { pattern -> lowerUrl.contains(pattern) }) return true

        // Check if any keyword appears after / or ? or & or = in lowerUrl
        if (authKeywords.any { kw ->
            lowerUrl.contains("/$kw") || lowerUrl.contains("?$kw") || lowerUrl.contains("&$kw") || lowerUrl.contains("=$kw")
        }) {
            return true
        }

        return false
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
