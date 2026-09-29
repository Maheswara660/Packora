package com.maheswara660.packora.manager

import android.content.Context
import com.maheswara660.packora.ui.InstalledPackoraApp
import com.maheswara660.packora.ui.detectInstalledPackoraApps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Thread-safe in-memory cache and background manager for installed Packora apps.
 * Scans once on application startup to ensure instant UI transitions across
 * MyApps and Updates screens without repeated package manager queries.
 */
object InstalledAppsManager {
    private val _installedApps = MutableStateFlow<List<InstalledPackoraApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledPackoraApp>> = _installedApps.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _hasScannedOnce = MutableStateFlow(false)
    val hasScannedOnce: StateFlow<Boolean> = _hasScannedOnce.asStateFlow()

    private val scanScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Triggers a scan of installed Packora apps.
     * If [force] is false and an initial scan has already completed during this session,
     * the call returns immediately to avoid unnecessary work.
     */
    fun scanApps(context: Context, force: Boolean = false) {
        if (!force && _hasScannedOnce.value) return

        scanScope.launch {
            _isScanning.value = true
            try {
                val appContext = context.applicationContext
                val historyManager = BuildHistoryManager(appContext)
                val detected = detectInstalledPackoraApps(appContext, historyManager)
                withContext(Dispatchers.Main) {
                    _installedApps.value = detected
                    _hasScannedOnce.value = true
                }
            } catch (_: Exception) {
            } finally {
                withContext(Dispatchers.Main) {
                    _isScanning.value = false
                }
            }
        }
    }

    /**
     * Removes an uninstalled app from the cached list immediately.
     */
    fun removeApp(packageName: String) {
        _installedApps.value = _installedApps.value.filter { it.packageName != packageName }
    }

    /**
     * Manually updates the cached list.
     */
    fun updateAppsList(newList: List<InstalledPackoraApp>) {
        _installedApps.value = newList
    }
}
