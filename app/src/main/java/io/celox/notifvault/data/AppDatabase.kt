package io.celox.notifvault.data

import androidx.room.Database
import androidx.room.RoomDatabase

// v2 introduces the stable CapturedMessage.conversationKey used for grouping. There is no
// 1→2 migration on purpose: the old rows were grouped by the unreliable title, so we take a
// clean slate (see DatabaseProvider.fallbackToDestructiveMigration) and let every chat
// re-populate with a proper stable key from now on.
@Database(entities = [CapturedMessage::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
