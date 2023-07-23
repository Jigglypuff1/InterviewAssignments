package com.example.interviewassignments.feature.linesonmap.presentation

import androidx.fragment.app.Fragment
import com.example.core_utils.di.DaggerViewModelFactory
import com.example.core_utils.di.fragmentViewModel
import dagger.Component
import dagger.Module
import dagger.Provides


@Module
class LinesOnMapModule(
    private val fragment: Fragment,
) {
    @Provides
    fun provideViewModel(
        factory: DaggerViewModelFactory<LinesOnMapViewModelImpl>,
    ): LinesOnMapViewModel = fragmentViewModel(fragment, factory)
}

@Component(
    modules = [LinesOnMapModule::class],
)
interface LinesOnMapComponent {
    fun inject(fragment: LinesOnMapFragment)
}

fun createLinesOnMapComponent(
    fragment: LinesOnMapFragment,
): LinesOnMapComponent = DaggerLinesOnMapComponent.builder()
    .linesOnMapModule(LinesOnMapModule(fragment))
    .build()
