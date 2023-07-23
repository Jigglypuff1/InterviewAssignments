package com.example.core_utils.livedata

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData

inline fun <T> Fragment.observeNonNullOnView(data: LiveData<T>, crossinline observer: (T) -> Unit) {
    data.observe(viewLifecycleOwner) {
        if (it != null)
            observer.invoke(it)
    }
}