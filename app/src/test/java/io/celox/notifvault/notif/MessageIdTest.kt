package io.celox.notifvault.notif

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * The content id is the de-duplication key — the whole reason a deleted WhatsApp message
 * survives. These tests pin its behavior so a refactor can't silently re-duplicate the vault.
 */
class MessageIdTest {

    @Test
    fun `is deterministic for identical content`() {
        val a = messageContentId("com.whatsapp", "Alice", "Alice", "Hallo Welt", 1_700_000_000_000)
        val b = messageContentId("com.whatsapp", "Alice", "Alice", "Hallo Welt", 1_700_000_000_000)
        assertEquals(a, b)
    }

    @Test
    fun `matches the pinned SHA-256 of the joined fields`() {
        // Guards the exact field order + '|' separator. Re-deliveries only collapse if this is stable.
        assertEquals(
            "4addf220379b0a67863512a63de094f6013fce60f1cdbbdf3a9dd95aaf39ad24",
            messageContentId("com.whatsapp", "Alice", "Alice", "Hallo Welt", 1_700_000_000_000)
        )
    }

    @Test
    fun `is a 64-char lowercase hex string`() {
        val id = messageContentId("p", "c", "s", "t", 1)
        assertEquals(64, id.length)
        assert(id.all { it in '0'..'9' || it in 'a'..'f' }) { "not lowercase hex: $id" }
    }

    @Test
    fun `differs when any field differs`() {
        val base = messageContentId("com.whatsapp", "Alice", "Alice", "Hi", 1000)
        assertNotEquals(base, messageContentId("org.telegram", "Alice", "Alice", "Hi", 1000)) // package
        assertNotEquals(base, messageContentId("com.whatsapp", "Bob", "Alice", "Hi", 1000))     // conversation
        assertNotEquals(base, messageContentId("com.whatsapp", "Alice", "Bob", "Hi", 1000))     // sender
        assertNotEquals(base, messageContentId("com.whatsapp", "Alice", "Alice", "Ho", 1000))   // text
        assertNotEquals(base, messageContentId("com.whatsapp", "Alice", "Alice", "Hi", 1001))   // time
    }

    @Test
    fun `field boundaries are not ambiguous`() {
        // "a|b" + "c" must not collide with "a" + "b|c" — the separator placement matters.
        assertNotEquals(
            messageContentId("a", "b", "x", "y", 1),
            messageContentId("a|b", "", "x", "y", 1)
        )
    }
}
