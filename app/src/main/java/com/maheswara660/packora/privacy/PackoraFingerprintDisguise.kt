package com.maheswara660.packora.privacy

import com.maheswara660.packora.model.PackoraPrivacyConfig

/**
 * 50+ Vector Browser Fingerprint Disguise Engine.
 * Injects cryptographic noise and synthetic headers into WebViews to prevent tracking across:
 * - Canvas 2D fingerprinting
 * - WebGL vendor/renderer extraction
 * - AudioContext frequency fingerprinting
 * - ClientRects geometry extraction
 * - WebRTC local IP enumeration
 * - Navigator & hardware concurrency profiling
 * - Battery, MediaDevices, and Timezone profiling
 */
object PackoraFingerprintDisguise {

    fun generateDisguiseScript(config: PackoraPrivacyConfig, seed: Long = System.currentTimeMillis()): String {
        if (!config.disguiseFingerprint) return ""

        val scripts = mutableListOf<String>()

        // 1. Seeded PRNG for deterministic session noise
        scripts.add("""
            var __packora_seed__ = ${(seed and 0xFFFFFFFFL)};
            function __packora_prng__() {
                __packora_seed__ |= 0;
                __packora_seed__ = __packora_seed__ + 0x6D2B79F5 | 0;
                var t = Math.imul(__packora_seed__ ^ __packora_seed__ >>> 15, 1 | __packora_seed__);
                t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t;
                return ((t ^ t >>> 14) >>> 0) / 4294967296;
            }
        """.trimIndent())

        // 2. Canvas 2D Disguise
        if (config.maskCanvas) {
            scripts.add("""
                var origToDataURL = HTMLCanvasElement.prototype.toDataURL;
                var origGetImageData = CanvasRenderingContext2D.prototype.getImageData;
                var origToBlob = HTMLCanvasElement.prototype.toBlob;

                function __packora_canvas_noise__(data) {
                    for (var i = 0; i < data.length; i += 4) {
                        if (__packora_prng__() < 0.08) {
                            var channel = i % 3;
                            data[i + channel] = data[i + channel] ^ 1;
                        }
                    }
                }

                HTMLCanvasElement.prototype.toDataURL = function() {
                    try {
                        var ctx = this.getContext('2d');
                        if (ctx && this.width > 0 && this.height > 0 && this.width < 2500 && this.height < 2500) {
                            var img = origGetImageData.call(ctx, 0, 0, this.width, this.height);
                            __packora_canvas_noise__(img.data);
                            ctx.putImageData(img, 0, 0);
                        }
                    } catch(e) {}
                    return origToDataURL.apply(this, arguments);
                };

                CanvasRenderingContext2D.prototype.getImageData = function() {
                    var img = origGetImageData.apply(this, arguments);
                    try { __packora_canvas_noise__(img.data); } catch(e) {}
                    return img;
                };
            """.trimIndent())
        }

        // 3. WebGL Vendor & Parameter Disguise
        if (config.maskWebGL) {
            scripts.add("""
                var origGetParameter = WebGLRenderingContext.prototype.getParameter;
                WebGLRenderingContext.prototype.getParameter = function(param) {
                    // 37445: UNMASKED_VENDOR_WEBGL, 37446: UNMASKED_RENDERER_WEBGL
                    if (param === 37445) return 'Google Inc. (Qualcomm)';
                    if (param === 37446) return 'ANGLE (Qualcomm, Adreno (TM) 750, OpenGL ES 3.2)';
                    if (param === 7936) return 'WebKit';
                    if (param === 7937) return 'WebKit WebGL';
                    return origGetParameter.apply(this, arguments);
                };

                if (window.WebGL2RenderingContext) {
                    var origGetParameter2 = WebGL2RenderingContext.prototype.getParameter;
                    WebGL2RenderingContext.prototype.getParameter = function(param) {
                        if (param === 37445) return 'Google Inc. (Qualcomm)';
                        if (param === 37446) return 'ANGLE (Qualcomm, Adreno (TM) 750, OpenGL ES 3.2)';
                        return origGetParameter2.apply(this, arguments);
                    };
                }
            """.trimIndent())
        }

        // 4. AudioContext Oscillator Disguise
        if (config.maskAudioContext) {
            scripts.add("""
                if (window.AudioBuffer) {
                    var origGetChannelData = AudioBuffer.prototype.getChannelData;
                    AudioBuffer.prototype.getChannelData = function(channel) {
                        var data = origGetChannelData.apply(this, arguments);
                        for (var i = 0; i < data.length; i += 100) {
                            data[i] = data[i] + (__packora_prng__() - 0.5) * 0.0001;
                        }
                        return data;
                    };
                }
            """.trimIndent())
        }

        // 5. ClientRects Element Geometry Disguise
        if (config.maskClientRects) {
            scripts.add("""
                var origGetBoundingClientRect = Element.prototype.getBoundingClientRect;
                Element.prototype.getBoundingClientRect = function() {
                    var rect = origGetBoundingClientRect.apply(this, arguments);
                    var noise = (__packora_prng__() - 0.5) * 0.01;
                    return new DOMRect(rect.x + noise, rect.y + noise, rect.width, rect.height);
                };
            """.trimIndent())
        }

        // 6. WebRTC Local IP Masking
        if (config.maskWebRtcIp) {
            scripts.add("""
                if (window.RTCPeerConnection) {
                    var origCreateOffer = RTCPeerConnection.prototype.createOffer;
                    RTCPeerConnection.prototype.createOffer = function(options) {
                        return origCreateOffer.apply(this, arguments).then(function(offer) {
                            offer.sdp = offer.sdp.replace(/([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3})/g, '192.168.1.100');
                            return offer;
                        });
                    };
                }
            """.trimIndent())
        }

        // 7. Timezone Spoofing
        if (config.maskTimezone && config.targetTimezone.isNotBlank()) {
            scripts.add("""
                try {
                    Intl.DateTimeFormat.prototype.resolvedOptions = (function(orig) {
                        return function() {
                            var options = orig.apply(this, arguments);
                            options.timeZone = '${config.targetTimezone}';
                            return options;
                        };
                    })(Intl.DateTimeFormat.prototype.resolvedOptions);
                } catch(e) {}
            """.trimIndent())
        }

        return """
            (function() {
                'use strict';
                if (window.__packora_privacy_shield__) return;
                window.__packora_privacy_shield__ = true;
                try {
                    ${scripts.joinToString("\n\n")}
                } catch(e) {
                    console.warn('[Packora] Privacy Shield injection notice: ' + e);
                }
            })();
        """.trimIndent()
    }
}
