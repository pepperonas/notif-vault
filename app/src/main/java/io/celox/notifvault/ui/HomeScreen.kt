package io.celox.notifvault.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.celox.notifvault.data.ConversationSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: VaultViewModel,
    onOpenConversation: (String, String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val conversations by vm.conversations.collectAsStateWithLifecycle()
    val results by vm.searchResults.collectAsStateWithLifecycle()
    val total by vm.totalCount.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NotifVault") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, "Einstellungen")
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; vm.setQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                placeholder = { Text("Nachrichten durchsuchen…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            if (query.isNotBlank()) {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(results, key = { it.id }) { m ->
                        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("${m.sender} · ${m.appLabel}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary)
                            Text(m.text, style = MaterialTheme.typography.bodyMedium)
                            Text(formatTimestamp(m.messageTime),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary)
                        }
                        HorizontalDivider()
                    }
                }
            } else if (conversations.isEmpty()) {
                EmptyState(total)
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(conversations, key = { it.conversation + it.packageName }) { c ->
                        ConversationRow(c) { onOpenConversation(c.conversation, c.packageName) }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(c: ConversationSummary, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (c.isGroup) Icons.Default.Groups else Icons.Default.Person,
            null, tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(c.conversation, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text("${c.messageCount}", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
            }
            Text(c.lastText, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary)
            Text("${c.appLabel} · ${formatTimestamp(c.lastTime)}",
                style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun EmptyState(total: Int) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Inbox, null, tint = MaterialTheme.colorScheme.secondary)
            Text("Noch keine Nachrichten gesichert", style = MaterialTheme.typography.titleMedium)
            Text("Sobald eine WhatsApp-Benachrichtigung eingeht, taucht sie hier auf.",
                style = MaterialTheme.typography.bodySmall)
            if (total > 0) Text("Gespeichert: $total")
        }
    }
}
