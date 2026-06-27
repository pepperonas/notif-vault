package io.celox.notifvault.service

import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import io.celox.notifvault.data.DatabaseProvider
import io.celox.notifvault.data.SettingsStore
import io.celox.notifvault.notif.MessageExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Receives every notification posted on the device. We persist messages from the
 * monitored apps the moment they arrive — so even if the sender later deletes the
 * message in WhatsApp (which sends no removal notification), our copy survives.
 */
class NotificationCaptureService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var extractor: MessageExtractor
    private lateinit var settings: SettingsStore

    override fun onCreate() {
        super.onCreate()
        extractor = MessageExtractor(packageManager)
        settings = SettingsStore(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn ?: return
        scope.launch {
            val captureAll = settings.captureAll.first()
            val monitored = settings.monitoredPackages.first()
            if (!captureAll && notification.packageName !in monitored) return@launch

            val result = extractor.extract(notification)
            val dao = DatabaseProvider.get(applicationContext).messageDao()
            if (result.messages.isNotEmpty()) dao.insertAll(result.messages)
            // A deleted-while-unread message arrived as a placeholder: flag the stored original.
            for (d in result.deletions) dao.markDeleted(d.conversationKey, d.sender, d.messageTime)
        }
    }

    // WhatsApp does not post a notification when a message is deleted, so onNotificationRemoved
    // is intentionally not used for capture — the original is already safely stored.

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Snapshot anything currently in the shade on (re)connect.
        activeNotifications?.forEach { onNotificationPosted(it) }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        // Samsung One UI aggressively kills listeners; ask the system to rebind us.
        requestRebind(ComponentName(this, NotificationCaptureService::class.java))
    }
}
