package io.github.henry753951.airplayremote.runtime

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Puts the TV back into standby after a cast that woke it ends, so a phone or Mac switching the TV
 * on does not leave it on in an empty room. It only acts when nobody has used the TV since:
 * a key press, leaving the receiver screen or a new connection cancels it.
 */
internal object CastAutoSleep {
    const val GRACE_MS = 30_000L

    private val mutableDeadline = MutableStateFlow<Long?>(null)
    /** elapsedRealtime when the TV will go to standby; null when nothing is pending. */
    val deadline: StateFlow<Long?> = mutableDeadline.asStateFlow()

    /** The receiver screen is in front (set by the activity). */
    @Volatile var uiVisible = false

    /** Someone pressed a key since the cast woke the TV; never switch it off under them. */
    @Volatile var userPresent = false
        private set

    fun noteUserPresent() {
        userPresent = true
        mutableDeadline.value = null
    }

    internal fun begin() {
        userPresent = false
        mutableDeadline.value = null
    }

    internal fun arm(deadline: Long) {
        mutableDeadline.value = deadline
    }

    internal fun disarm() {
        mutableDeadline.value = null
    }
}

/** The decisions, kept free of Android so they can be unit tested. */
internal object CastAutoSleepPolicy {
    /** At the end of the last connection: count down only for a cast-started, untouched TV on our screen. */
    fun shouldArm(wokeForCast: Boolean, enabled: Boolean, userPresent: Boolean, uiVisible: Boolean) =
        wokeForCast && enabled && !userPresent && uiVisible

    /** At the deadline: still nobody here, still nothing connected, still on our screen. */
    fun shouldSleep(deadline: Long?, now: Long, clients: Int, userPresent: Boolean, uiVisible: Boolean) =
        deadline != null && now >= deadline && clients == 0 && !userPresent && uiVisible
}
