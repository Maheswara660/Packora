package com.maheswara660.packora.web

external val window: dynamic

val globalState = PackoraWebSimulatorState()

@OptIn(ExperimentalJsExport::class)
@JsExport
fun getPackoraVersion(): String = "5.4.0"

@OptIn(ExperimentalJsExport::class)
@JsExport
fun getPackoraTargets(): Array<String> = WebAppTarget.values().map { "${it.id}|${it.title}|${it.badge}|${it.description}" }.toTypedArray()

@OptIn(ExperimentalJsExport::class)
@JsExport
fun getDohResolvers(): Array<String> = dohResolvers.map { "${it.id}|${it.name}|${it.endpoint}|${it.features}" }.toTypedArray()

@OptIn(ExperimentalJsExport::class)
@JsExport
fun getPrivacyVectors(): Array<String> = PrivacyEngine.vectors.map { "${it.category}|${it.vectorName}|${it.attackSurface}|${it.mitigation}" }.toTypedArray()

@OptIn(ExperimentalJsExport::class)
@JsExport
fun getSimulationPipeline(targetId: String, url: String, appName: String, pkgName: String): Array<String> {
    val tempState = PackoraWebSimulatorState().apply {
        selectedTarget = WebAppTarget.values().find { it.id == targetId } ?: WebAppTarget.WEB
        targetUrl = url
        this.appName = appName
        packageName = pkgName
    }
    return CompilerSimulator.getPipeline(tempState).map { "${it.stepNumber}|${it.title}|${it.detail}|${it.durationMs}" }.toTypedArray()
}

fun main() {
    val exportObj = js("({})")
    exportObj.version = getPackoraVersion()
    exportObj.getTargets = ::getPackoraTargets
    exportObj.getResolvers = ::getDohResolvers
    exportObj.getPrivacyVectors = ::getPrivacyVectors
    exportObj.getPipeline = ::getSimulationPipeline

    window.PackoraKMP = exportObj
    println("Packora Kotlin Multiplatform Engine v5.4.0 initialized successfully.")
}
