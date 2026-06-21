package io.celox.notifvault

import android.app.Application
import io.celox.notifvault.data.DatabaseProvider

class NotifVaultApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Warm up the encrypted DB so the listener can write immediately.
        DatabaseProvider.get(this)
    }
}
