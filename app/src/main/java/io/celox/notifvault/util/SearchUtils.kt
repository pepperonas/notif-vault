package io.celox.notifvault.util

/**
 * Escape SQL LIKE metacharacters so that a literal `%` or `_` typed by the user is matched
 * verbatim instead of acting as a wildcard. Paired with `ESCAPE '\'` in `MessageDao.search`.
 * The backslash itself must be escaped first, otherwise it would consume the following escape.
 */
fun escapeLike(query: String): String =
    query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

/**
 * Case-insensitive, non-overlapping match ranges of [query] (trimmed) within [text].
 * Uses `indexOf(ignoreCase = true)` (regionMatches-based, length-preserving) — computing
 * indices on a `lowercase()` copy breaks when case folding changes the string length
 * (e.g. 'İ' U+0130 lowercases to two code units), shifting highlights or crashing.
 */
fun findMatches(text: String, query: String): List<IntRange> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    val out = mutableListOf<IntRange>()
    var i = 0
    while (true) {
        val idx = text.indexOf(q, i, ignoreCase = true)
        if (idx < 0) break
        out += idx until idx + q.length
        i = idx + q.length
    }
    return out
}
