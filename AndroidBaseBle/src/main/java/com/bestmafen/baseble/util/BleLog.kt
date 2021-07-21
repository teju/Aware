package com.bestmafen.baseble.util

import android.util.Log

/**
 * 原生蓝牙回调：I
 * 框架蓝牙回调：D
 */
object BleLog {
    private const val TAG = "BaseBle"

    var mInterceptor: BleLogInterceptor? = null

    fun v(msg: String) {
        if (mInterceptor != null && mInterceptor!!(LogLevel.V, TAG, msg)) return

        Log.v(TAG, msg)
    }

    fun d(msg: String) {
        if (mInterceptor != null && mInterceptor!!(LogLevel.D, TAG, msg)) return

        Log.d(TAG, msg)
    }

    fun i(msg: String) {
        if (mInterceptor != null && mInterceptor!!(LogLevel.I, TAG, msg)) return

        Log.i(TAG, msg)
    }

    fun w(msg: String) {
        if (mInterceptor != null && mInterceptor!!(LogLevel.W, TAG, msg)) return

        Log.w(TAG, msg)
    }

    fun e(msg: String) {
        if (mInterceptor != null && mInterceptor!!(LogLevel.E, TAG, msg)) return

        Log.e(TAG, msg)
    }
}

/**
 * 日志拦截器，用来自定义日志输出，如果返回true，会禁用内置的日志输出
 */
typealias BleLogInterceptor = (level: LogLevel, tag: String, msg: String) -> Boolean

enum class LogLevel {
    V, D, I, W, E
}