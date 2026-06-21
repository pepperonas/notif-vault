package io.celox.notifvault.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.celox.notifvault.data.CapturedMessage
import io.celox.notifvault.data.ConversationSummary
import io.celox.notifvault.data.DatabaseProvider
import io.celox.notifvault.data.MessageDao
import io.celox.notifvault.data.SettingsStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModel(app: Application) : AndroidViewModel(app) {

    private val dao: MessageDao = DatabaseProvider.get(app).messageDao()
    val settings = SettingsStore(app)

    val conversations: StateFlow<List<ConversationSummary>> =
        dao.conversations().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCount: StateFlow<Int> =
        dao.count().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val query = MutableStateFlow("")

    val searchResults: StateFlow<List<CapturedMessage>> = query
        .flatMapLatest { q -> if (q.isBlank()) flowOf(emptyList()) else dao.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) { query.value = q }

    fun messagesFor(conversation: String, pkg: String) =
        dao.messagesFor(conversation, pkg)

    fun deleteConversation(conversation: String, pkg: String) = viewModelScope.launch {
        dao.deleteConversation(conversation, pkg)
    }

    fun clearAll() = viewModelScope.launch { dao.clear() }

    suspend fun exportAll() = dao.exportAll()

    fun setCaptureAll(value: Boolean) = viewModelScope.launch { settings.setCaptureAll(value) }
    fun setMonitored(packages: Set<String>) = viewModelScope.launch { settings.setMonitored(packages) }
    fun setBiometric(value: Boolean) = viewModelScope.launch { settings.setBiometricLock(value) }
}
