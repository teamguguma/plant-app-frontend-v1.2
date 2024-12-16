import android.content.Context
import java.util.UUID
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object UUIDManager {
    private const val PREF_NAME = "user_prefs"
    private const val KEY_UUID = "user_uuid"

    fun getOrCreateUUID(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val uuid = prefs.getString(KEY_UUID, null)
        return if (uuid == null) {
            val newUUID = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_UUID, newUUID).apply()
            newUUID
        } else {
            uuid
        }
    }
}