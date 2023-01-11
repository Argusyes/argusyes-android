package com.android.argusyes.utils

fun Float.formatPrint(): String {
    return if (this >= 100) {
        this.toInt().toString()
    } else {
        this.toString()
    }
}