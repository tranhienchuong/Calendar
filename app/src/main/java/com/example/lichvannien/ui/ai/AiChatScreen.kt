package com.example.lichvannien.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lichvannien.R
import com.example.lichvannien.theme.*

@Composable
fun AiChatScreen(
    modifier: Modifier = Modifier,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Scroll to bottom when messages update
    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.text) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    val isDark = isSystemInDarkTheme()
    val aiBubbleBg = if (isDark) AiChatBubbleAiDark else AiChatBubbleAiLight

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Title: "Trò chuyện AI"
        Text(
            text = stringResource(R.string.title_ai_screen),
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Suggestion Chips
        val suggestions = listOf(
            "🔮 Tử vi hôm nay",
            "⭐ Giờ đẹp xuất hành",
            "🗓️ Lập lịch cuộc họp",
            "🎋 Xem ngày tốt xấu"
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestions) { suggestion ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.clickable { viewModel.sendMessage(suggestion) }
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                ChatMessageItem(
                    message = message,
                    aiBubbleBg = aiBubbleBg
                )
            }

            if (state.isTyping && (state.messages.isEmpty() || state.messages.last().text.isNotBlank())) {
                item(key = "typing_indicator") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = AppHeaderBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI đang phản hồi...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.inputText,
                onValueChange = { viewModel.onInputChanged(it) },
                placeholder = { Text(stringResource(R.string.ai_input_hint), fontSize = 14.sp) },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppHeaderBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            IconButton(
                onClick = { viewModel.sendMessage() },
                enabled = state.inputText.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (state.inputText.isNotBlank()) AppHeaderBlue else MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Gửi",
                    tint = if (state.inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    aiBubbleBg: Color,
    modifier: Modifier = Modifier
) {
    if (message.sender == MessageSender.AI) {
        // AI Message: Left Aligned with Bot Avatar
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppHeaderBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint = AppHeaderBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = aiBubbleBg,
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    if (message.text.isBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = AppHeaderBlue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Đang soạn câu trả lời...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        FormattedAiMessageText(
                            text = message.text,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    } else {
        // User Message: Right Aligned, Blue Container
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 4.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = AiChatBubbleUser,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text(
                    text = message.text,
                    fontSize = 14.5.sp,
                    lineHeight = 21.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}

/**
 * Rich Markdown formatter for Jetpack Compose that eliminates raw markdown symbols
 * (**, ###, ---, etc.) and renders clean, readable bold styling, headers, dividers, and bullet lists.
 */
@Composable
fun FormattedAiMessageText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val lines = remember(text) { text.lines() }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        lines.forEach { rawLine ->
            val trimmed = rawLine.trim()
            when {
                trimmed == "---" || trimmed == "***" -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
                trimmed.startsWith("### ") -> {
                    val content = trimmed.removePrefix("### ").trim()
                    Text(
                        text = parseMarkdownToAnnotatedString(content),
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppHeaderBlue,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    val content = trimmed.removePrefix("## ").trim()
                    Text(
                        text = parseMarkdownToAnnotatedString(content),
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppHeaderBlue,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("# ") -> {
                    val content = trimmed.removePrefix("# ").trim()
                    Text(
                        text = parseMarkdownToAnnotatedString(content),
                        fontSize = 17.5.sp,
                        fontWeight = FontWeight.Black,
                        color = AppHeaderBlue,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    val itemText = trimmed.substring(2).trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp)
                    ) {
                        Text(
                            text = "• ",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppHeaderBlue
                        )
                        Text(
                            text = parseMarkdownToAnnotatedString(itemText),
                            fontSize = 14.5.sp,
                            lineHeight = 21.sp,
                            color = color
                        )
                    }
                }
                trimmed.matches(Regex("""^\d+\.\s+.*""")) -> {
                    val match = Regex("""^(\d+\.)\s+(.*)""").find(trimmed)
                    if (match != null) {
                        val num = match.groupValues[1]
                        val itemContent = match.groupValues[2]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                        ) {
                            Text(
                                text = "$num ",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppHeaderBlue
                            )
                            Text(
                                text = parseMarkdownToAnnotatedString(itemContent),
                                fontSize = 14.5.sp,
                                lineHeight = 21.sp,
                                color = color
                            )
                        }
                    } else {
                        Text(
                            text = parseMarkdownToAnnotatedString(trimmed),
                            fontSize = 14.5.sp,
                            lineHeight = 21.sp,
                            color = color
                        )
                    }
                }
                trimmed.isBlank() -> {
                    Spacer(modifier = Modifier.height(2.dp))
                }
                else -> {
                    Text(
                        text = parseMarkdownToAnnotatedString(trimmed),
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp,
                        color = color
                    )
                }
            }
        }
    }
}

/**
 * Converts inline markdown (like **bold text**) into an AnnotatedString with bold SpanStyle.
 */
fun parseMarkdownToAnnotatedString(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    val boldContent = text.substring(i + 2, end)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(boldContent)
                    }
                    i = end + 2
                    continue
                }
            }
            append(text[i])
            i++
        }
    }
}
