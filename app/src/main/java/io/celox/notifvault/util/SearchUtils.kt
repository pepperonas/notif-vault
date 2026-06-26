package io.celox.notifvault.util

/**
 * Escape SQL LIKE metacharacters so that a literal `%` or `_` typed by the user is matched
 * verbatim instead of acting as a wildcard. Paired with `ESCAPE '\'` in `MessageDao.search`.
 * The backslash itself must be escaped first, otherwise it would consume the following escape.
 */
fun escapeLike(query: String): String =
    query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
