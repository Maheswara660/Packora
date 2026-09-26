package com.maheswara660.packora.extension

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.maheswara660.packora.builder.AppLogger
import java.util.concurrent.ConcurrentHashMap

/**
 * Greasemonkey / Tampermonkey Userscript Bridge for Packora.
 * Polyfills `GM_getValue`, `GM_setValue`, `GM_deleteValue`, `GM_listValues`, `GM_addStyle`,
 * `GM_log`, and promise-based `GM.*` APIs in WebViews.
 */
class PackoraUserScriptEngine(private val webView: WebView) {

    companion object {
        private const val TAG = "PackoraUserScriptEngine"
        private const val JS_BRIDGE_NAME = "__packora_gm_bridge__"
    }

    private val scriptStorage = ConcurrentHashMap<String, String>()

    init {
        webView.addJavascriptInterface(this, JS_BRIDGE_NAME)
    }

    @JavascriptInterface
    fun getValue(key: String, defaultValue: String?): String? =
        scriptStorage[key] ?: defaultValue

    @JavascriptInterface
    fun setValue(key: String, value: String) {
        scriptStorage[key] = value
    }

    @JavascriptInterface
    fun deleteValue(key: String) {
        scriptStorage.remove(key)
    }

    @JavascriptInterface
    fun log(message: String) {
        AppLogger.d(TAG, "[UserScript] $message")
    }

    /**
     * Injects the GM_* polyfill wrapper and then executes the user's `.user.js` script safely.
     */
    fun injectUserScript(scriptContent: String) {
        val wrappedScript = """
            (function() {
                'use strict';
                if (!window.GM) {
                    window.GM = {};
                }

                // GM_* Legacy Polyfills
                window.GM_setValue = function(key, val) {
                    window.$JS_BRIDGE_NAME.setValue(key, JSON.stringify(val));
                };

                window.GM_getValue = function(key, defaultVal) {
                    var stored = window.$JS_BRIDGE_NAME.getValue(key, null);
                    if (stored === null) return defaultVal;
                    try { return JSON.parse(stored); } catch(e) { return stored; }
                };

                window.GM_deleteValue = function(key) {
                    window.$JS_BRIDGE_NAME.deleteValue(key);
                };

                window.GM_log = function(msg) {
                    window.$JS_BRIDGE_NAME.log(String(msg));
                };

                window.GM_addStyle = function(css) {
                    var style = document.createElement('style');
                    style.textContent = css;
                    (document.head || document.documentElement).appendChild(style);
                };

                // GM.* Promise Polyfills
                window.GM.getValue = function(k, d) { return Promise.resolve(window.GM_getValue(k, d)); };
                window.GM.setValue = function(k, v) { window.GM_setValue(k, v); return Promise.resolve(); };
                window.GM.deleteValue = function(k) { window.GM_deleteValue(k); return Promise.resolve(); };

                // Execute User Script
                try {
                    $scriptContent
                } catch(e) {
                    console.error('[Packora UserScript Error]', e);
                }
            })();
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(wrappedScript, null)
        }
    }
}
