package io.celox.notifvault.notif

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import io.celox.notifvault.data.CapturedMessage

/**
 * Converts a posted notification into zero or more [CapturedMessage]s.
 *
 * Strategy:
 *  1. Skip the group-summary notification ("5 new messages from 3 chats") to avoid noise.
 *  2. Prefer MessagingStyle: WhatsApp/Signal/etc. embed each individual message with its
 *     real sender + timestamp. This is far more accurate than reading EXTRA_TITLE/TEXT and
 *     also recovers the short back-history bundled into each notification.
 *  3. Fall back to title/text (and inbox-style text lines) for apps without MessagingStyle.
 *
 * The de-dup key is a content hash, so the same message arriving inside many subsequent
 * notifications collapses to one stored row.
 */
class MessageExtractor(private val pm: PackageManager) {

    private val deletionMarkers = listOf(
        "this message was deleted",
        "diese nachricht wurde gelöscht",
        "you deleted this message",
        "du hast diese nachricht gelöscht"
    )

    fun extract(sbn: StatusBarNotification): List<CapturedMessage> {
        val n = sbn.notification ?: return emptyList()
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return emptyList()

        val pkg = sbn.packageName
        val appLabel = labelFor(pkg)
        val now = System.currentTimeMillis()
        // Stable per-chat identifier, independent of the (often-missing) display title.
        // WhatsApp & co. post one re-used notification per chat: its conversation shortcut id —
        // or, failing that, its tag (the chat's JID) — is constant for that chat.
        val stableKey = runCatching { n.shortcutId }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: sbn.tag?.takeIf { it.isNotBlank() }
        val out = mutableListOf<CapturedMessage>()

        val style = runCatching {
            NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(n)
        }.getOrNull()

        if (style != null && style.messages.isNotEmpty()) {
            val convTitle = style.conversationTitle?.toString()?.trim()?.takeIf { it.isNotEmpty() }
            val isGroup = style.isGroupConversation
            for (m in style.messages) {
                val text = m.text?.toString()?.trim().orEmpty()
                if (text.isEmpty()) continue
                val sender = m.person?.name?.toString()
                    ?: style.user.name?.toString()
                    ?: "Unbekannt"
                // Title for display: group name if present, else the 1:1 contact (the sender).
                val title = convTitle ?: sender
                // Group key: stable id if we have one, else fall back to the title (legacy behavior).
                val key = stableKey ?: title
                val time = if (m.timestamp > 0) m.timestamp else sbn.postTime
                out += build(pkg, appLabel, key, title, sender, isGroup, text, time, now)
            }
        } else {
            val extras = n.extras
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
            val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()
            val convTitle = title?.takeIf { it.isNotEmpty() } ?: appLabel
            val key = stableKey ?: convTitle

            if (!lines.isNullOrEmpty()) {
                for (line in lines) {
                    val t = line?.toString()?.trim().orEmpty()
                    if (t.isEmpty()) continue
                    out += build(pkg, appLabel, key, convTitle, convTitle, false, t, sbn.postTime, now)
                }
            } else if (!text.isNullOrEmpty()) {
                out += build(pkg, appLabel, key, convTitle, convTitle, false, text, sbn.postTime, now)
            }
        }

        return out
    }

    private fun build(
        pkg: String, appLabel: String, conversationKey: String, conversation: String, sender: String,
        isGroup: Boolean, text: String, time: Long, capturedAt: Long
    ): CapturedMessage {
        // De-dup on the stable key (+ sender/text/time) so re-deliveries collapse even when the
        // notification's displayed title differs between posts.
        val id = messageContentId(pkg, conversationKey, sender, text, time)
        val suspected = deletionMarkers.any { text.lowercase().contains(it) }
        return CapturedMessage(
            id = id,
            packageName = pkg,
            appLabel = appLabel,
            conversationKey = conversationKey.ifBlank { conversation.ifBlank { appLabel } },
            conversation = conversation.ifBlank { appLabel },
            sender = sender.ifBlank { "Unbekannt" },
            isGroup = isGroup,
            text = text,
            messageTime = time,
            capturedAt = capturedAt,
            deletionSuspected = suspected
        )
    }

    private fun labelFor(pkg: String): String = runCatching {
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault(pkg)
}
