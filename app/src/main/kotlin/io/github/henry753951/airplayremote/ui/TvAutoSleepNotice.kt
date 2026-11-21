package io.github.henry753951.airplayremote.ui

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.henry753951.airplayremote.R
import io.github.henry753951.airplayremote.runtime.CastAutoSleep
import kotlinx.coroutines.delay

/**
 * The quiet warning before the TV returns to standby after a cast that switched it on.
 * Any key keeps the TV on (the activity cancels on every key), so the button only makes that obvious.
 */
@Composable
internal fun TvAutoSleepNotice() {
    val deadline by CastAutoSleep.deadline.collectAsState()
    val active = deadline
    // the countdown ticks only while the notice is up, then stops
    var secondsLeft by remember { mutableLongStateOf(0L) }
    LaunchedEffect(active) {
        while (active != null) {
            secondsLeft = ((active - SystemClock.elapsedRealtime() + 999) / 1000).coerceAtLeast(0)
            if (secondsLeft == 0L) break
            delay(1000L - (SystemClock.elapsedRealtime() % 1000))
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = active != null,
            enter = fadeIn(tween(220)) + slideInVertically(tween(260)) { it / 4 },
            exit = fadeOut(tween(160)) + slideOutVertically(tween(200)) { it / 4 }
        ) {
            val keepFocus = remember { FocusRequester() }
            LaunchedEffect(Unit) { keepFocus.requestFocusUntilLanded() }
            Row(
                Modifier
                    .padding(bottom = 48.dp)
                    .widthIn(max = 720.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xF21D1D20))
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f, fill = false)) {
                    Text(stringResource(R.string.auto_sleep_title), color = TvText, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    Text(
                        stringResource(R.string.auto_sleep_body, secondsLeft.toInt()),
                        color = TvMuted, fontSize = 16.sp, lineHeight = 23.sp
                    )
                }
                Spacer(Modifier.width(24.dp))
                KeepOnButton(Modifier.focusRequester(keepFocus))
            }
        }
    }
}

@Composable
private fun KeepOnButton(modifier: Modifier) {
    var focused by remember { mutableStateOf(false) }
    val bg by animateColorAsState(if (focused) TvText else Color(0x2EFFFFFF), tween(FOCUS_MS), label = "keepBg")
    val fg by animateColorAsState(if (focused) TvInk else TvText, tween(FOCUS_MS), label = "keepFg")
    Box(
        modifier
            .onFocusChanged { focused = it.isFocused }
            .clip(CircleShape)
            .background(bg)
            .clickable(remember { MutableInteractionSource() }, indication = null, role = Role.Button) {
                CastAutoSleep.noteUserPresent()
            }
            .heightIn(min = 48.dp)
            .padding(horizontal = 26.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(R.string.auto_sleep_keep_on), color = fg, fontSize = 17.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}
