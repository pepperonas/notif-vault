package io.celox.notifvault.notif

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeletionTest {

    @Test
    fun `recognizes WhatsApp deleted placeholders in both languages`() {
        assertTrue(isDeletionPlaceholder("This message was deleted"))
        assertTrue(isDeletionPlaceholder("Diese Nachricht wurde gelöscht"))
        assertTrue(isDeletionPlaceholder("You deleted this message"))
        assertTrue(isDeletionPlaceholder("Du hast diese Nachricht gelöscht"))
    }

    @Test
    fun `is case-insensitive and tolerates an emoji or prefix`() {
        assertTrue(isDeletionPlaceholder("THIS MESSAGE WAS DELETED"))
        assertTrue(isDeletionPlaceholder("🚫 This message was deleted"))
    }

    @Test
    fun `does not flag ordinary messages`() {
        assertFalse(isDeletionPlaceholder("Hallo, wie geht's?"))
        assertFalse(isDeletionPlaceholder("Ich habe die Datei gelöscht."))
        assertFalse(isDeletionPlaceholder(""))
    }
}
