package com.turo.nibel.sample

import android.app.Application
import com.turo.nibel.sample.ui.RootContent
import dagger.hilt.android.HiltAndroidApp
import nibel.runtime.Nibel

@HiltAndroidApp
class NibelSampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Nibel.configure(rootDelegate = RootContent())
    }
}
