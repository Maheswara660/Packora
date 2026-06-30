package com.maheswara660.packora.builder

import android.util.Log

object AppLogger {
    fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }
    fun d(tag: String, msg: String, tr: Throwable) {
        Log.d(tag, msg, tr)
    }
    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }
    fun w(tag: String, msg: String) {
        Log.w(tag, msg)
    }
    fun w(tag: String, msg: String, tr: Throwable) {
        Log.w(tag, msg, tr)
    }
    fun e(tag: String, msg: String) {
        Log.e(tag, msg)
    }
    fun e(tag: String, msg: String, tr: Throwable) {
        Log.e(tag, msg, tr)
    }
}
