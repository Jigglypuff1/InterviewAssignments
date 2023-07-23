package com.example.core_utils.ui

import android.content.res.Configuration
import androidx.fragment.app.Fragment

fun Fragment.isNightModeEnabled() =
    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_NO -> false
        Configuration.UI_MODE_NIGHT_YES -> true
        else -> false
    }