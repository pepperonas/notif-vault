package io.celox.notifvault.util

import org.junit.Assert.assertEquals
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
}
