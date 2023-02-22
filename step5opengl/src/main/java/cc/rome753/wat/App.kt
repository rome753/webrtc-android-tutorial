package cc.rome753.wat

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
    }

    companion object {
        var app : App? = null
    }
}