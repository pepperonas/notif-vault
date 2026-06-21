package io.celox.notifvault.util

import io.celox.notifvault.data.CapturedMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY)

    fun toCsv(messages: List<CapturedMessage>): String {
        val sb = StringBuilder("Zeit;App;Chat;Absender;Gruppe;Text\n")
        for (m in messages) {
            sb.append(fmt.format(Date(m.messageTime))).append(';')
                .append(esc(m.appLabel)).append(';')
                .append(esc(m.conversation)).append(';')
                .append(esc(m.sender)).append(';')
                .append(if (m.isGroup) "ja" else "nein").append(';')
                .append(esc(m.text)).append('\n')
        }
        return sb.toString()
    }

    fun toJson(messages: List<CapturedMessage>): String {
        val sb = StringBuilder("[\n")
        messages.forEachIndexed { i, m ->
            sb.append("  {")
                .append("\"time\":\"${fmt.format(Date(m.messageTime))}\",")
                .append("\"app\":\"${j(m.appLabel)}\",")
                .append("\"chat\":\"${j(m.conversation)}\",")
                .append("\"sender\":\"${j(m.sender)}\",")
                .append("\"group\":${m.isGroup},")
                .append("\"text\":\"${j(m.text)}\"")
                .append("}")
            if (i < messages.lastIndex) sb.append(",")
            sb.append("\n")
        }
        sb.append("]\n")
        return sb.toString()
    }

    private fun esc(s: String): String =
        if (s.contains(';') || s.contains('"') || s.contains('\n'))
            "\"" + s.replace("\"", "\"\"").replace("\n", " ") + "\""
        else s

    private fun j(s: String): String =
        s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
}
