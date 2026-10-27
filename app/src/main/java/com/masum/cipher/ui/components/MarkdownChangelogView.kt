package com.masum.cipher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.ui.theme.Typography

@Composable
fun MarkdownChangelogView(
    markdownText: String,
    modifier: Modifier = Modifier
) {
    val lines = markdownText.lines()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            when {
                trimmed.startsWith("### ") -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trimmed.removePrefix("### ").trim(),
                        style = Typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.removePrefix("## ").trim(),
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    val bulletContent = trimmed.substring(2).trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(5.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {}
                        Text(
                            text = parseInlineBold(bulletContent),
                            style = Typography.bodySmall.copy(
                                lineHeight = 18.sp,
                                fontSize = 12.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    Text(
                        text = parseInlineBold(trimmed),
                        style = Typography.bodySmall.copy(
                            lineHeight = 18.sp,
                            fontSize = 12.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun parseInlineBold(text: String) = buildAnnotatedString {
    var cursor = 0
    val boldPattern = "\\*\\*(.*?)\\*\\*".toRegex()
    val matches = boldPattern.findAll(text)

    for (match in matches) {
        if (match.range.first > cursor) {
            append(text.substring(cursor, match.range.first))
        }
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
            append(match.groupValues[1])
        }
        cursor = match.range.last + 1
    }

    if (cursor < text.length) {
        append(text.substring(cursor))
    }
}
