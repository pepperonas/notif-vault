package io.celox.notifvault

import android.app.Application
import io.celox.notifvault.data.DatabaseProvider

class NotifVaultApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Warm up the encrypted DB so the listener can write immediately — but off the main
        // thread: Keystore + EncryptedSharedPreferences work would otherwise delay app start.
        // DatabaseProvider.get is synchronized, so a racing caller simply waits for this one.
        Thread { DatabaseProvider.get(this) }.start()
    }
}
