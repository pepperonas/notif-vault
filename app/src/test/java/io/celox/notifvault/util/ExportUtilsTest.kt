package io.celox.notifvault.util

import io.celox.notifvault.data.CapturedMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportUtilsTest {

    private fun msg(
        text: String,
        isGroup: Boolean = false,
        conversation: String = "Alice",
        sender: String = "Alice",
        appLabel: String = "WhatsApp"
    ) = CapturedMessage(
        id = "id-${text.hashCode()}-$isGroup",
        packageName = "com.whatsapp",
        appLabel = appLabel,
        conversationKey = "key-$conversation",
        conversation = conversation,
        sender = sender,
        isGroup = isGroup,
        text = text,
        messageTime = 1_700_000_000_000,
        capturedAt = 1_700_000_000_000
    )

    // ---- CSV ----

    @Test
    fun `csv starts with the header row`() {
        assertTrue(ExportUtils.toCsv(emptyList()).startsWith("Zeit;App;Chat;Absender;Gruppe;Text\n"))
    }

    @Test
    fun `csv of empty list is header only`() {
        assertEquals("Zeit;App;Chat;Absender;Gruppe;Text\n", ExportUtils.toCsv(emptyList()))
    }

    @Test
    fun `csv leaves plain fields unquoted and maps the group flag`() {
        val csv = ExportUtils.toCsv(listOf(msg("Hallo", isGroup = false)))
        assertTrue(csv.contains(";WhatsApp;Alice;Alice;nein;Hallo"))
        val group = ExportUtils.toCsv(listOf(msg("Hi", isGroup = true)))
        assertTrue(group.contains(";ja;Hi"))
    }

    @Test
    fun `csv quotes fields containing the separator`() {
        val csv = ExportUtils.toCsv(listOf(msg("preis;menge")))
        assertTrue(csv.contains("\"preis;menge\""))
    }

    @Test
    fun `csv doubles embedded quotes`() {
        val csv = ExportUtils.toCsv(listOf(msg("sag \"hallo\"")))
        assertTrue(csv.contains("\"sag \"\"hallo\"\"\""))
    }

    @Test
    fun `csv flattens newlines to spaces so rows stay intact`() {
        val csv = ExportUtils.toCsv(listOf(msg("zeile1\nzeile2")))
        assertTrue(csv.contains("\"zeile1 zeile2\""))
        // exactly one data row → header newline + one row newline = 2 line breaks total
        assertEquals(2, csv.count { it == '\n' })
    }

    // ---- JSON ----

    @Test
    fun `json is a bracketed array`() {
        val json = ExportUtils.toJson(listOf(msg("x"))).trim()
        assertTrue(json.startsWith("["))
        assertTrue(json.endsWith("]"))
    }

    @Test
    fun `json of empty list is an empty array`() {
        assertEquals("[\n]\n", ExportUtils.toJson(emptyList()))
    }

    @Test
    fun `json renders the group flag as a boolean literal`() {
        assertTrue(ExportUtils.toJson(listOf(msg("x", isGroup = true))).contains("\"group\":true"))
        assertTrue(ExportUtils.toJson(listOf(msg("x", isGroup = false))).contains("\"group\":false"))
    }

    @Test
    fun `json escapes quotes and backslashes`() {
        assertTrue(ExportUtils.toJson(listOf(msg("a\"b"))).contains("\"text\":\"a\\\"b\""))
        assertTrue(ExportUtils.toJson(listOf(msg("a\\b"))).contains("\"text\":\"a\\\\b\""))
    }

    @Test
    fun `json escapes newlines and drops carriage returns`() {
        val json = ExportUtils.toJson(listOf(msg("a\r\nb")))
        assertTrue(json.contains("a\\nb"))   // \r removed, \n -> \n
        assertFalse(json.contains("\r"))
    }

    @Test
    fun `json separates multiple objects with commas`() {
        val json = ExportUtils.toJson(listOf(msg("one"), msg("two")))
        assertEquals(1, json.split("},").size - 1)
    }
}
