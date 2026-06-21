package io.celox.notifvault.ui

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val time = SimpleDateFormat("HH:mm", Locale.GERMANY)
private val dateTime = SimpleDateFormat("dd.MM.yy HH:mm", Locale.GERMANY)

fun formatTimestamp(millis: Long): String {
    val cal = Calendar.getInstance()
    val today = cal.get(Calendar.DAY_OF_YEAR)
    val year = cal.get(Calendar.YEAR)
    cal.timeInMillis = millis
    return if (cal.get(Calendar.DAY_OF_YEAR) == today && cal.get(Calendar.YEAR) == year)
        "Heute ${time.format(Date(millis))}"
    else dateTime.format(Date(millis))
}
