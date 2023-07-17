package com.turo.nibel.sample

import android.app.Application
import com.turo.nibel.runtime.Nibel
import com.turo.nibel.sample.ui.RootContent
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NibelSampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Nibel.configure(rootDelegate = RootContent())
    }
}
