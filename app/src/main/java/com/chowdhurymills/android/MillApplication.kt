package com.chowdhurymills.android

import android.app.Application
import com.chowdhurymills.android.data.MillApi
import com.chowdhurymills.android.data.SessionStore

class MillApplication : Application() {
    lateinit var sessionStore: SessionStore
        private set
    lateinit var api: MillApi
        private set

    override fun onCreate() {
        super.onCreate()
        sessionStore = SessionStore(this)
        api = MillApi(sessionStore)
    }
}
