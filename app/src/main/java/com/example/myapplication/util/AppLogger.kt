package com.example.myapplication.util

import android.util.Log

object AppLogger {
    private const val PREFIX = "MyApp"

    fun d(tag: String, message: String) {
        Log.d("$PREFIX:$tag", message)
    }

    fun i(tag: String, message: String) {
        Log.i("$PREFIX:$tag", message)
    }

    fun w(tag: String, message: String) {
        Log.w("$PREFIX:$tag", message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e("$PREFIX:$tag", message, throwable)
    }
}
