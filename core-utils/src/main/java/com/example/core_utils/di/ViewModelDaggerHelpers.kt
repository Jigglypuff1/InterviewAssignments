package com.example.core_utils.di

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("unused")
inline fun <reified ViewModelT, reified ViewModelImplT : ViewModel> fragmentViewModel(
    fragment: Fragment,
    factory: DaggerViewModelFactory<ViewModelImplT>,
): ViewModelT = ViewModelProvider(fragment, factory)[ViewModelImplT::class.java].also {
    if (it is LifecycleObserver)
        fragment.lifecycle.addObserver(it)
} as ViewModelT
