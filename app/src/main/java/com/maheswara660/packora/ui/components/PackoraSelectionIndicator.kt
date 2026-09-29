package com.maheswara660.packora.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom Packora selection indicator to replace standard Android RadioButton widgets.
 * Features an authentic animated check circle badge with 200ms easing and theme-adaptive colors.
 */
@Composable
fun PackoraSelectionIndicator(
    selected: Boolean,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
) {
    val interactionSource = remember { MutableInteractionSource() }

    val borderColor by animateColorAsState(
        targetValue = if (selected) selectedColor else unselectedColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "IndicatorBorderColor"
    )

    val bgColor by animateColorAsState(
        targetValue = if (selected) selectedColor else Color.Transparent,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "IndicatorBgColor"
    )

    val checkScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "IndicatorCheckScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(
                width = if (selected) 2.dp else 1.5.dp,
                color = borderColor,
                shape = CircleShape
            )
            .background(bgColor, CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checkScale > 0f) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(size * 0.65f)
                    .scale(checkScale)
            )
        }
    }
}
