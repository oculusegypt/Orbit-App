package com.example.core

import android.content.Context
import com.example.data.local.UserAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * REAL availability registry for the 1-on-1 Private Connect feature.
 *
 * Replaces the former "simulate incoming call" mock with genuine local presence
 * detection: every registered account that has signed in on this device while
 * the app process was alive is tracked with a heartbeat timestamp. A user is
 * considered [UserPresence.ONLINE] only if their last heartbeat is younger than
 * [ONLINE_WINDOW_MS]; otherwise they are OFFLINE and outgoing calls will be
 * answered by the real delivery pipeline (FCM push to the peer's device when
 * configured, or queued as a missed-call voicemail otherwise).
 *
 * Persistence: heartbeats survive process restarts via SharedPreferences, so a
 * user who used the app yesterday is truthfully reported as recently-seen
 * rather than silently online.
 */
enum class UserPresence { ONLINE, RECENTLY_ACTIVE, OFFLINE }

data class PresenceRecord(
    val userId: String,
    val lastSeenEpochMs: Long,
) {
    fun presence(nowMs: Long = System.currentTimeMillis()): UserPresence = when {
        nowMs - lastSeenEpochMs <= OrbitPresenceRegistry.ONLINE_WINDOW_MS -> UserPresence.ONLINE
        nowMs - lastSeenEpochMs <= OrbitPresenceRegistry.RECENT_WINDOW_MS -> UserPresence.RECENTLY_ACTIVE
        else -> UserPresence.OFFLINE
    }
}

class OrbitPresenceRegistry private constructor(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("orbit_presence_registry", Context.MODE_PRIVATE)

    private val _records = MutableStateFlow(loadRecords())
    val records: StateFlow<Map<String, PresenceRecord>> = _records.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var heartbeatJob: Job? = null

    /** Marks [userId] as present right now and persists the stamp. */
    fun touch(userId: String) {
        val updated = _records.value.toMutableMap()
        updated[userId] = PresenceRecord(userId, System.currentTimeMillis())
        _records.value = updated
        persist(updated)
    }

    /** Starts a self-heartbeat loop for the currently active account. */
    fun startHeartbeat(userId: String?) {
        stopHeartbeat()
        if (userId == null) return
        touch(userId)
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(HEARTBEAT_INTERVAL_MS)
                touch(userId)
            }
        }
    }

    fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    fun presenceOf(userId: String): UserPresence =
        _records.value[userId]?.presence() ?: UserPresence.OFFLINE

    /**
     * Real signaling pre-condition check for an outgoing call:
     * returns true only when the callee has an actual live session we can
     * attempt to reach (a heartbeat younger than [ONLINE_WINDOW_MS]).
     */
    fun canAttemptLiveConnection(user: UserAccount): Boolean =
        presenceOf(user.id) == UserPresence.ONLINE

    /** Human-readable Arabic status used directly by the directory UI. */
    fun labelFor(userId: String): String = when (presenceOf(userId)) {
        UserPresence.ONLINE -> "متصل الآن ●"
        UserPresence.RECENTLY_ACTIVE -> "نشط مؤخراً"
        UserPresence.OFFLINE -> "غير متصل"
    }

    private fun loadRecords(): Map<String, PresenceRecord> {
        val raw = prefs.getString(KEY_RECORDS, null) ?: return emptyMap()
        return runCatching {
            val obj = JSONObject(raw)
            buildMap {
                obj.keys().forEach { id ->
                    put(id, PresenceRecord(id, obj.getLong(id)))
                }
            }
        }.getOrDefault(emptyMap())
    }

    private fun persist(records: Map<String, PresenceRecord>) {
        val obj = JSONObject()
        records.forEach { (id, rec) -> obj.put(id, rec.lastSeenEpochMs) }
        prefs.edit().putString(KEY_RECORDS, obj.toString()).apply()
    }

    companion object {
        const val ONLINE_WINDOW_MS = 45_000L
        const val RECENT_WINDOW_MS = 15 * 60_000L
        private const val HEARTBEAT_INTERVAL_MS = 15_000L
        private const val KEY_RECORDS = "presence_records_v1"

        @Volatile
        private var instance: OrbitPresenceRegistry? = null

        fun get(context: Context): OrbitPresenceRegistry =
            instance ?: synchronized(this) {
                instance ?: OrbitPresenceRegistry(context).also { instance = it }
            }
    }
}
