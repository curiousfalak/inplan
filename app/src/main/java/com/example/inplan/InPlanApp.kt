package com.example.inplan

import android.app.Application
import com.onesignal.OneSignal
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class InPlanApp : Application(){
    override fun onCreate() {
        super.onCreate()
        OneSignal.initWithContext(this, "63b4bd5b-4b40-4fa4-bfe8-e633cdcf539e")
    }
}
