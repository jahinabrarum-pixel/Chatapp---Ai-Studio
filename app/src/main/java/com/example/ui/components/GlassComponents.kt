package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.VyroAccentPrimary
import com.example.ui.theme.VyroAccentSecondary
import com.example.ui.theme.VyroBackground
import com.example.ui.theme.VyroBackgroundSecondary
import com.example.ui.theme.VyroGlassBorder
import com.example.ui.theme.VyroGlassSurface
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary

@Composable
fun AmbientGlowBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VyroBackground)
            .drawBehind {
                // Soft ambient top-left violet glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(VyroAccentPrimary.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.1f),
                        radius = size.width * 0.7f
                    )
                )
                // Soft ambient bottom-right cyan glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(VyroAccentSecondary.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.85f),
                        radius = size.width * 0.65f
                    )
                )
            }
    ) {
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = VyroGlassSurface,
    borderColor: Color = VyroGlassBorder,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    listOf(
                        borderColor.copy(alpha = 0.35f),
                        borderColor.copy(alpha = 0.08f),
                        borderColor.copy(alpha = 0.25f)
                    )
                ),
                shape = shape
            )
    ) {
        content()
    }
}

@Composable
fun VyroGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(listOf(VyroAccentPrimary, VyroAccentSecondary))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF333A4D), Color(0xFF282D3D)))
                }
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) Color.White else VyroTextSecondary
            )
        }
    }
}

@Composable
fun VyroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = VyroTextSecondary.copy(alpha = 0.6f)) },
        singleLine = singleLine,
        maxLines = maxLines,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = VyroBackgroundSecondary.copy(alpha = 0.7f),
            unfocusedContainerColor = VyroBackgroundSecondary.copy(alpha = 0.4f),
            focusedBorderColor = VyroAccentPrimary,
            unfocusedBorderColor = VyroGlassBorder,
            focusedTextColor = VyroTextPrimary,
            unfocusedTextColor = VyroTextPrimary,
            focusedLabelColor = VyroAccentPrimary,
            unfocusedLabelColor = VyroTextSecondary
        ),
        modifier = modifier.fillMaxWidth()
    )
}
