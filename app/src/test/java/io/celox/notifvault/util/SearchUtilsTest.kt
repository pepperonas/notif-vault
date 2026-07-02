package io.celox.notifvault.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchUtilsTest {

    @Test
    fun `plain text is unchanged`() {
        assertEquals("hallo welt", escapeLike("hallo welt"))
    }

    @Test
    fun `percent is escaped so it matches literally`() {
        assertEquals("100\\% sicher", escapeLike("100% sicher"))
    }

    @Test
    fun `underscore is escaped`() {
        assertEquals("user\\_name", escapeLike("user_name"))
    }

    @Test
    fun `backslash is escaped first so it cannot eat the following escape`() {
        // A literal backslash must become "\\" before % / _ get their own "\" prefix.
        assertEquals("a\\\\b", escapeLike("a\\b"))
        assertEquals("\\\\\\%", escapeLike("\\%")) // input: backslash + percent
    }

    @Test
    fun `multiple metacharacters are all escaped`() {
        assertEquals("\\%\\_\\%", escapeLike("%_%"))
    }

    @Test
    fun `empty stays empty`() {
        assertEquals("", escapeLike(""))
    }

    // ---- findMatches (search-result highlighting) ----

    @Test
    fun `findMatches finds all case-insensitive occurrences`() {
        assertEquals(listOf(0..4, 10..14), findMatches("Hallo und hallo", "hallo"))
    }

    @Test
    fun `findMatches matches umlauts case-insensitively`() {
        assertEquals(listOf(0..5), findMatches("MÜLLER kommt", "müller"))
    }

    @Test
    fun `findMatches survives length-changing case folding`() {
        // 'İ' (U+0130) lowercases to two code units — index math on lowercase() copies
        // shifted highlights or crashed. Ranges must stay within the original string.
        val text = "İstanbul İzmir"
        val ranges = findMatches(text, "izmir")
        for (r in ranges) assertTrue(r.last < text.length)
        // No crash and no false mid-word offsets for a query later in the string.
        val hello = findMatches("İİİ hallo", "hallo")
        assertEquals(listOf(4..8), hello)
    }

    @Test
    fun `findMatches with blank query yields nothing`() {
        assertEquals(emptyList<IntRange>(), findMatches("text", "   "))
        assertEquals(emptyList<IntRange>(), findMatches("text", ""))
    }

    @Test
    fun `findMatches trims the query like the search field does`() {
        assertEquals(listOf(5..8), findMatches("gute nacht", " nach "))
    }

    @Test
    fun `findMatches without a hit is empty`() {
        assertEquals(emptyList<IntRange>(), findMatches("hallo", "xyz"))
    }
}
