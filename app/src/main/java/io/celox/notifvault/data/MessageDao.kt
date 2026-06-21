package io.celox.notifvault.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Lightweight projection for the conversation overview list. */
data class ConversationSummary(
    val conversation: String,
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

    @Query(
        """
        SELECT conversation, packageName, appLabel, isGroup,
               text AS lastText, MAX(messageTime) AS lastTime, COUNT(*) AS messageCount
        FROM messages
        GROUP BY conversation, packageName
        ORDER BY lastTime DESC
        """
    )
    fun conversations(): Flow<List<ConversationSummary>>

    @Query(
        """
        SELECT * FROM messages
        WHERE conversation = :conversation AND packageName = :pkg
        ORDER BY messageTime ASC
        """
    )
    fun messagesFor(conversation: String, pkg: String): Flow<List<CapturedMessage>>

    @Query(
        """
        SELECT * FROM messages
        WHERE text LIKE '%' || :q || '%'
           OR sender LIKE '%' || :q || '%'
           OR conversation LIKE '%' || :q || '%'
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

    @Query("DELETE FROM messages WHERE conversation = :conversation AND packageName = :pkg")
    suspend fun deleteConversation(conversation: String, pkg: String)
}
