package io.celox.notifvault.notif

import java.security.MessageDigest

/**
 * The de-duplication key: identical message content always maps to the same id, so when
 * WhatsApp re-posts a message inside many successive MessagingStyle notifications the
 * `INSERT … OR IGNORE` collapses them to a single row. This is the heart of the app, so
 * the exact field order and separator are pinned by [MessageIdTest] — changing them would
 * silently re-duplicate every already-stored message.
 */
internal fun messageContentId(
    packageName: String,
    conversation: String,
    sender: String,
    text: String,
    messageTime: Long
): String = sha256("$packageName|$conversation|$sender|$text|$messageTime")

private fun sha256(s: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
