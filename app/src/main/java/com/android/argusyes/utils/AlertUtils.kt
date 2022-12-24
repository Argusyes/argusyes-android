package com.android.argusyes.utils

import android.content.Context
import android.widget.Toast

class AlertUtils {

    companion object {
        fun alter(message: String, context: Context?) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}