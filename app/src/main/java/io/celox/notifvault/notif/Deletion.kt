package io.celox.notifvault.notif

// The exact placeholder texts a messenger puts into the *updated* notification when a
// still-unread message is deleted. We match on `contains` (lowercased) so an emoji/prefix
// like "🚫 This message was deleted" is still recognised; the phrases are distinctive
// enough not to collide with normal conversation.
private val DELETION_MARKERS = listOf(
    "this message was deleted",
    "diese nachricht wurde gelöscht",
    "you deleted this message",
    "du hast diese nachricht gelöscht"
)

/** True if [text] is a "this message was deleted" placeholder (any known language). */
fun isDeletionPlaceholder(text: String): Boolean {
    val t = text.lowercase()
    return DELETION_MARKERS.any { t.contains(it) }
}
