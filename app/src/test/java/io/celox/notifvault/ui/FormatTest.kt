package io.celox.notifvault.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class FormatTest {

    private fun at(year: Int, month0: Int, day: Int, hour: Int, min: Int): Long {
        val c = Calendar.getInstance()
        c.set(year, month0, day, hour, min, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    // ---- initials ----

    @Test
    fun `single name yields one initial`() {
        assertEquals("A", initials("Alice"))
    }

    @Test
    fun `full name yields first and last initials`() {
        assertEquals("AM", initials("Alice Müller"))
        assertEquals("JD", initials("John Michael Doe")) // first + last, middle ignored
    }

    @Test
    fun `surrounding and inner whitespace is collapsed`() {
        assertEquals("JD", initials("   john    doe  "))
    }

    @Test
    fun `initials are uppercased with German locale`() {
        assertEquals("Ä", initials("ärzte"))
    }

    @Test
    fun `blank or empty falls back to question mark`() {
        assertEquals("?", initials(""))
        assertEquals("?", initials("   "))
    }

    // ---- dayKey ----

    @Test
    fun `same calendar day maps to the same key`() {
        assertEquals(
            dayKey(at(2024, Calendar.JUNE, 15, 9, 0)),
            dayKey(at(2024, Calendar.JUNE, 15, 9, 0))
        )
        assertEquals(
            dayKey(at(2024, Calendar.JUNE, 15, 0, 1)),
            dayKey(at(2024, Calendar.JUNE, 15, 23, 59))
        )
    }

    @Test
    fun `different days map to different keys`() {
        assertNotEquals(
            dayKey(at(2024, Calendar.JUNE, 15, 23, 59)),
            dayKey(at(2024, Calendar.JUNE, 16, 0, 1))
        )
    }

    @Test
    fun `dayKey is the start of that day`() {
        val noon = at(2024, Calendar.JUNE, 15, 12, 0)
        val key = dayKey(noon)
        assertEquals(at(2024, Calendar.JUNE, 15, 0, 0), key)
        assertTrue(key <= noon)
        assertTrue(noon - key < 24L * 60 * 60 * 1000)
    }

    // ---- formatClock ----

    @Test
    fun `clock is formatted as HH colon mm`() {
        assertTrue(formatClock(at(2024, Calendar.JUNE, 15, 9, 5)).matches(Regex("\\d{2}:\\d{2}")))
    }

    // ---- identityColor ----

    @Test
    fun `identity color is deterministic per name`() {
        assertEquals(identityColor("Alice Müller"), identityColor("Alice Müller"))
        assertEquals(identityColor("WhatsApp Gruppe"), identityColor("WhatsApp Gruppe"))
    }

    @Test
    fun `different names spread across more than one palette color`() {
        val names = listOf("Alice", "Bob", "Carol", "Dave", "Erin", "Frank", "Grace", "Heidi")
        val distinct = names.map { identityColor(it) }.toSet()
        assertTrue("expected variety, got $distinct", distinct.size > 1)
    }
}
