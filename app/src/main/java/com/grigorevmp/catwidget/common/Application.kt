package com.grigorevmp.catwidget.common

import android.app.Application
import android.util.Log
import com.grigorevmp.catwidget.utils.Preferences
import com.grigorevmp.catwidget.utils.Utils


class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d("Application", "Application created")

        Utils.init(this)
        Utils.initSharedPreferences()

        Preferences.init(this)
        Preferences.initSharedPreferences()
    }

    init {
        instance = this
    }


    companion object {
        lateinit var instance: Application
    }
}