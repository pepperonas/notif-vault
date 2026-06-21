package io.celox.notifvault.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    vm: VaultViewModel,
    conversation: String,
    pkg: String,
    onBack: () -> Unit
) {
    // remember keyed by chat so the Flow isn't rebuilt (and the collection reset) on every recomposition.
    val messages by remember(conversation, pkg) { vm.messagesFor(conversation, pkg) }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(conversation, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.deleteConversation(conversation, pkg); onBack() }) {
                        Icon(Icons.Default.DeleteOutline, "Chat löschen")
                    }
                }
            )
        }
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize().padding(horizontal = 12.dp),
        ) {
            items(messages, key = { it.id }) { m ->
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = if (m.deletionSuspected)
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    else CardDefaults.cardColors()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        if (m.isGroup) {
                            Text(m.sender, style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                        Text(m.text, style = MaterialTheme.typography.bodyLarge)
                        Text(formatTimestamp(m.messageTime),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}
