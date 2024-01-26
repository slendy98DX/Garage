package com.example.garage

import android.app.Application
import android.content.Context

class TestApplication : Application() {

    override fun getApplicationContext(): Context {
        return this
    }
}
