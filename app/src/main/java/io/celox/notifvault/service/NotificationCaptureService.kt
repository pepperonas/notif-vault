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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    // Settings cached in-memory (null until DataStore's first emission) so we don't collect
    // the DataStore flow from scratch for every posted notification.
    private lateinit var captureAll: StateFlow<Boolean?>
    private lateinit var monitored: StateFlow<Set<String>?>

    // Notifications are processed strictly in post order through this queue: a deletion
    // placeholder must never be applied before the insert of the original it flags, and
    // concurrent per-notification coroutines (even on a single-threaded dispatcher) could
    // interleave at suspension points.
    private val queue = Channel<StatusBarNotification>(Channel.UNLIMITED)

    override fun onCreate() {
        super.onCreate()
        extractor = MessageExtractor(packageManager)
        settings = SettingsStore(applicationContext)
        captureAll = settings.captureAll.map { it as Boolean? }
            .stateIn(scope, SharingStarted.Eagerly, null)
        monitored = settings.monitoredPackages.map { it as Set<String>? }
            .stateIn(scope, SharingStarted.Eagerly, null)
        // runCatching: one bad notification must not kill the consumer loop for good.
        scope.launch { for (sbn in queue) runCatching { process(sbn) } }
    }

    override fun onDestroy() {
        queue.close()
        scope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn ?: return
        queue.trySend(notification)
    }

    private suspend fun process(sbn: StatusBarNotification) {
        // filterNotNull().first() waits for DataStore's initial load once, then is free.
        val all = captureAll.filterNotNull().first()
        val pkgs = monitored.filterNotNull().first()
        if (!all && sbn.packageName !in pkgs) return

        val result = extractor.extract(sbn)
        val dao = DatabaseProvider.get(applicationContext).messageDao()
        if (result.messages.isNotEmpty()) dao.insertAll(result.messages)
        // A deleted-while-unread message arrived as a placeholder: flag the stored original.
        for (d in result.deletions) dao.markDeleted(d.conversationKey, d.sender, d.messageTime)
    }

    // WhatsApp does not post a notification when a message is deleted, so onNotificationRemoved
    // is intentionally not used for capture — the original is already safely stored.

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Snapshot anything currently in the shade on (re)connect. activeNotifications can
        // throw if the listener is racing a disconnect.
        runCatching { activeNotifications }.getOrNull()?.forEach { onNotificationPosted(it) }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        // Samsung One UI aggressively kills listeners; ask the system to rebind us.
        requestRebind(ComponentName(this, NotificationCaptureService::class.java))
    }
}
