package com.example.core_utils.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class DaggerViewModelFactory<T : ViewModel> @Inject constructor(
    private val viewModel: dagger.Lazy<T>,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModel.get() as T
}
