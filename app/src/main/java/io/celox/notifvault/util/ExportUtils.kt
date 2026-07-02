package io.celox.notifvault.util

import io.celox.notifvault.data.CapturedMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    // SimpleDateFormat is not thread-safe — create one per export run instead of sharing.
    private fun dateFmt() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY)

    fun toCsv(messages: List<CapturedMessage>): String {
        val fmt = dateFmt()
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
        val fmt = dateFmt()
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
        if (s.contains(';') || s.contains('"') || s.contains('\n') || s.contains('\r'))
            "\"" + s.replace("\"", "\"\"").replace("\r\n", " ")
                .replace('\n', ' ').replace('\r', ' ') + "\""
        else s

    // JSON string escaping incl. all control characters (< U+0020) — a raw tab or CR in a
    // message would otherwise produce output that strict parsers reject.
    private fun j(s: String): String = buildString(s.length) {
        for (c in s) when {
            c == '\\' -> append("\\\\")
            c == '"' -> append("\\\"")
            c == '\n' -> append("\\n")
            c == '\r' -> append("\\r")
            c == '\t' -> append("\\t")
            c < ' ' -> append("\\u%04x".format(c.code))
            else -> append(c)
        }
    }
}
