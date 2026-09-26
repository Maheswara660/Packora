package com.maheswara660.packora.adblock

import android.net.Uri
import com.maheswara660.packora.model.PackoraAdBlockConfig
import java.util.concurrent.ConcurrentSkipListSet

/**
 * High-speed hosts and URL pattern ad-blocker with cosmetic DOM element suppression.
 */
object PackoraAdBlocker {

    private val defaultBlockedDomains = hashSetOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "ads.google.com",
        "admob.com",
        "facebook.com/tr",
        "pixel.facebook.com",
        "analytics.tiktok.com",
        "ads.tiktok.com",
        "c.amazon-adsystem.com",
        "aax.amazon-adsystem.com",
        "ads.twitter.com",
        "unityads.unity3d.com",
        "applovin.com",
        "ironsrc.mobi",
        "vungle.com",
        "chartboost.com",
        "adcolony.com",
        "inmobi.com",
        "tapjoy.com",
        "scorecardresearch.com",
        "quantserve.com",
        "outbrain.com",
        "taboola.com",
        "revcontent.com",
        "mgid.com",
        "popads.net",
        "adcash.com",
        "propellerads.com"
    )

    private val customBlockedDomains = ConcurrentSkipListSet<String>()

    fun applyConfig(config: PackoraAdBlockConfig) {
        customBlockedDomains.clear()
        if (config.enabled) {
            config.customHostsRules.forEach { rule ->
                val clean = rule.trim().lowercase().removePrefix("0.0.0.0 ").removePrefix("127.0.0.1 ")
                if (clean.isNotBlank() && !clean.startsWith("#")) {
                    customBlockedDomains.add(clean)
                }
            }
        }
    }

    fun isAdUrl(url: String, config: PackoraAdBlockConfig): Boolean {
        if (!config.enabled) return false

        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false
        val host = uri.host?.lowercase() ?: return false

        // Check custom list first
        if (customBlockedDomains.contains(host)) return true

        // Check default tracker & ad network lists
        if (config.blockTrackers) {
            for (blocked in defaultBlockedDomains) {
                if (host == blocked || host.endsWith(".$blocked")) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Generates a cosmetic CSS and MutationObserver script that hides ad containers on page load.
     */
    fun generateCosmeticFilterScript(): String {
        return """
            (function() {
                'use strict';
                if (window.__packora_cosmetic_filter__) return;
                window.__packora_cosmetic_filter__ = true;

                var adSelectors = [
                    'ins.adsbygoogle',
                    '[id^="google_ads_"]',
                    '[id^="div-gpt-ad"]',
                    '.ad-container',
                    '.ad-wrapper',
                    '.ad-banner',
                    '.advertisement',
                    '[data-ad-unit]',
                    '[aria-label="advertisement"]'
                ];

                var style = document.createElement('style');
                style.textContent = adSelectors.join(', ') + ' { display: none !important; visibility: hidden !important; height: 0 !important; }';
                (document.head || document.documentElement).appendChild(style);

                function removeAds() {
                    adSelectors.forEach(function(sel) {
                        document.querySelectorAll(sel).forEach(function(el) {
                            el.style.setProperty('display', 'none', 'important');
                        });
                    });
                }

                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', removeAds);
                } else {
                    removeAds();
                }

                // Clean dynamic injected ads
                var observer = new MutationObserver(function() {
                    removeAds();
                });
                observer.observe(document.documentElement, { childList: true, subtree: true });
            })();
        """.trimIndent()
    }
}
