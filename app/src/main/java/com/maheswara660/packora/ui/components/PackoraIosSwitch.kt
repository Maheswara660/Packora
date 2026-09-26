package com.maheswara660.packora.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Authentic iOS-styled switch component with 51x31dp track, 27dp sliding thumb,
 * smooth 200ms easing, elevation shadows, and theme-adaptive colors.
 */
@Composable
fun PackoraIosSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    checkedTrackColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedTrackColor: Color = if (isSystemInDarkTheme()) Color(0xFF39393D) else Color(0xFFE9E9EB),
    thumbColor: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }

    val trackColor by animateColorAsState(
        targetValue = if (checked) checkedTrackColor else uncheckedTrackColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "IosSwitchTrackColor"
    )

    // Track width: 51.dp, height: 31.dp. Thumb: 27.dp. Inset: 2.dp.
    // Unchecked offset: 2.dp. Checked offset: 51 - 27 - 2 = 22.dp.
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "IosSwitchThumbOffset"
    )

    Box(
        modifier = modifier
            .size(width = 51.dp, height = 31.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) trackColor else trackColor.copy(alpha = 0.5f))
            .then(
                if (enabled && onCheckedChange != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        onCheckedChange(!checked)
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(27.dp)
                .shadow(elevation = 3.dp, shape = CircleShape)
                .background(if (enabled) thumbColor else thumbColor.copy(alpha = 0.7f), CircleShape)
        )
    }
}
