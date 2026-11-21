package io.github.henry753951.airplayremote.runtime
import org.junit.Assert.*
import org.junit.Test

class CastAutoSleepPolicyTest {
    @Test fun armsOnlyForAnUntouchedCastThatSwitchedTheTvOn() {
        assertTrue(CastAutoSleepPolicy.shouldArm(wokeForCast = true, enabled = true, userPresent = false, uiVisible = true))
        // TV was already on: someone is watching
        assertFalse(CastAutoSleepPolicy.shouldArm(wokeForCast = false, enabled = true, userPresent = false, uiVisible = true))
        assertFalse(CastAutoSleepPolicy.shouldArm(wokeForCast = true, enabled = false, userPresent = false, uiVisible = true))
        // a key press during the cast
        assertFalse(CastAutoSleepPolicy.shouldArm(wokeForCast = true, enabled = true, userPresent = true, uiVisible = true))
        // the viewer left for another app
        assertFalse(CastAutoSleepPolicy.shouldArm(wokeForCast = true, enabled = true, userPresent = false, uiVisible = false))
    }

    @Test fun sleepsOnlyAtTheDeadlineWithNothingChanged() {
        assertTrue(CastAutoSleepPolicy.shouldSleep(deadline = 1_000, now = 1_000, clients = 0, userPresent = false, uiVisible = true))
        assertFalse(CastAutoSleepPolicy.shouldSleep(deadline = 1_000, now = 999, clients = 0, userPresent = false, uiVisible = true))
        // cancelled
        assertFalse(CastAutoSleepPolicy.shouldSleep(deadline = null, now = 5_000, clients = 0, userPresent = false, uiVisible = true))
        // a new cast came in
        assertFalse(CastAutoSleepPolicy.shouldSleep(deadline = 1_000, now = 2_000, clients = 1, userPresent = false, uiVisible = true))
        assertFalse(CastAutoSleepPolicy.shouldSleep(deadline = 1_000, now = 2_000, clients = 0, userPresent = true, uiVisible = true))
        assertFalse(CastAutoSleepPolicy.shouldSleep(deadline = 1_000, now = 2_000, clients = 0, userPresent = false, uiVisible = false))
    }
}
