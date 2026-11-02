package com.example.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.components.GlassCard
import com.example.ui.components.VyroGradientButton
import com.example.ui.components.VyroTextField
import com.example.ui.theme.VyroAccentPrimary
import com.example.ui.theme.VyroAccentSecondary
import com.example.ui.theme.VyroBackgroundSecondary
import com.example.ui.theme.VyroGlassBorder
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onSubmitPost: (caption: String, mediaUrls: List<String>, location: String, tags: List<String>) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("Vyro, Cyber, Glass") }
    var selectedMediaUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=800&q=80") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Preset high-aesthetic images matching the 3D glass / cyber theme
    val presetImages = listOf(
        "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1634017839464-5c339ebe3cb4?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&w=800&q=80"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = VyroBackgroundSecondary,
            border = androidx.compose.foundation.BorderStroke(1.dp, VyroGlassBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create VYRO Post",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = VyroTextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = VyroTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Caption field
                VyroTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = "What's happening?",
                    placeholder = "Share your thoughts beyond limits...",
                    singleLine = false,
                    maxLines = 4,
                    modifier = Modifier.height(100.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Media Selector
                Text(
                    text = "Select Media Visual",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = VyroAccentSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(presetImages) { url ->
                        val isSelected = selectedMediaUrl == url
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) VyroAccentPrimary else VyroGlassBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedMediaUrl = url }
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Media option",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Location field
                VyroTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = "Location",
                    placeholder = "e.g. Neo Tokyo, Global, Cyberspace",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = VyroAccentSecondary
                        )
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tags field
                VyroTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = "Hashtags",
                    placeholder = "Comma-separated tags",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null,
                            tint = VyroAccentPrimary
                        )
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Submit button
                if (isSubmitting) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = VyroAccentPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    VyroGradientButton(
                        text = "Share Post",
                        onClick = {
                            if (caption.isNotBlank()) {
                                isSubmitting = true
                                val tagsList = tagsText.split(",").map { it.trim().removePrefix("#") }.filter { it.isNotBlank() }
                                val mediaList = if (selectedMediaUrl.isNotBlank()) listOf(selectedMediaUrl) else emptyList()
                                onSubmitPost(caption.trim(), mediaList, location.trim(), tagsList)
                            }
                        },
                        enabled = caption.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
