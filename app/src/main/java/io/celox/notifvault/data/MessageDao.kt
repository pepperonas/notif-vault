package io.celox.notifvault.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Lightweight projection for the conversation overview list. */
data class ConversationSummary(
    val conversationKey: String,
    val conversation: String,   // latest title for this chat
    val packageName: String,
    val appLabel: String,
    val isGroup: Boolean,
    val lastText: String,
    val lastTime: Long,
    val messageCount: Int
)

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(messages: List<CapturedMessage>): List<Long>

    // Group by the stable conversationKey. The bare columns (conversation, appLabel, isGroup,
    // text) resolve to the row holding MAX(messageTime) — SQLite's documented min/max-bare-column
    // behaviour — so the overview shows each chat's latest title and last message.
    @Query(
        """
        SELECT conversationKey, conversation, packageName, appLabel, isGroup,
               text AS lastText, MAX(messageTime) AS lastTime, COUNT(*) AS messageCount
        FROM messages
        GROUP BY conversationKey, packageName
        ORDER BY lastTime DESC
        """
    )
    fun conversations(): Flow<List<ConversationSummary>>

    @Query(
        """
        SELECT * FROM messages
        WHERE conversationKey = :conversationKey AND packageName = :pkg
        ORDER BY messageTime ASC
        """
    )
    fun messagesFor(conversationKey: String, pkg: String): Flow<List<CapturedMessage>>

    // ESCAPE '\' so % and _ typed by the user match literally
    // (the ViewModel backslash-escapes them before calling this).
    @Query(
        """
        SELECT * FROM messages
        WHERE text LIKE '%' || :q || '%' ESCAPE '\'
           OR sender LIKE '%' || :q || '%' ESCAPE '\'
           OR conversation LIKE '%' || :q || '%' ESCAPE '\'
        ORDER BY messageTime DESC
        LIMIT 500
        """
    )
    fun search(q: String): Flow<List<CapturedMessage>>

    @Query("SELECT * FROM messages ORDER BY messageTime DESC")
    suspend fun exportAll(): List<CapturedMessage>

    @Query("SELECT COUNT(*) FROM messages")
    fun count(): Flow<Int>

    @Query("DELETE FROM messages")
    suspend fun clear()

    @Query("DELETE FROM messages WHERE conversationKey = :conversationKey AND packageName = :pkg")
    suspend fun deleteConversation(conversationKey: String, pkg: String)
}
