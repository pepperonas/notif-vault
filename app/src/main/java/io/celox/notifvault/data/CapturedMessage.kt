package io.celox.notifvault.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single captured message. The [id] is a content hash so that WhatsApp re-posting
 * the same message inside many MessagingStyle notifications results in exactly one row
 * (insert uses IGNORE on conflict). That is the whole de-duplication trick.
 */
@Entity(
    tableName = "messages",
    indices = [Index("conversation"), Index("packageName"), Index("messageTime")]
)
data class CapturedMessage(
    @PrimaryKey val id: String,
    val packageName: String,
    val appLabel: String,
    val conversation: String,   // chat / group title
    val sender: String,         // who wrote it
    val isGroup: Boolean,
    val text: String,
    val messageTime: Long,      // original timestamp of the message
    val capturedAt: Long,       // when we stored it
    val deletionSuspected: Boolean = false
)
