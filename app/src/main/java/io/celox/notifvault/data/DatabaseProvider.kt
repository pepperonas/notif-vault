package io.celox.notifvault.data

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.security.SecureRandom

/**
 * Builds a single, AES-256 (SQLCipher) encrypted Room database.
 * The random 32-byte passphrase is generated once and stored inside
 * EncryptedSharedPreferences, which itself wraps values with AES-256-GCM
 * backed by the Android Keystore. The DB file on disk is unreadable without it.
 */
object DatabaseProvider {

    @Volatile private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase =
        instance ?: synchronized(this) {
            instance ?: build(context.applicationContext).also { instance = it }
        }

    private fun build(context: Context): AppDatabase {
        // Load native SQLCipher libs.
        System.loadLibrary("sqlcipher")

        val passphrase = passphrase(context)
        val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(context, AppDatabase::class.java, "vault.db")
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    private fun passphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            "nv_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val existing = prefs.getString(KEY_DB_PASS, null)
        if (existing != null) return existing.toByteArray(Charsets.ISO_8859_1)

        val raw = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val asString = String(raw, Charsets.ISO_8859_1)
        prefs.edit().putString(KEY_DB_PASS, asString).apply()
        return raw
    }

    private const val KEY_DB_PASS = "db_passphrase"
}
