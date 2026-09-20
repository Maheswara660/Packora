package com.maheswara660.packora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PriorityHigh
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Markdown AST Nodes ────────────────────────────────────────────────────────

sealed interface MarkdownNode {
    data class Header(val level: Int, val text: String) : MarkdownNode
    data class Paragraph(val text: String) : MarkdownNode
    data class CodeBlock(val language: String, val code: String) : MarkdownNode
    data class BlockQuote(val alertType: AlertType?, val text: String) : MarkdownNode
    data class ListItem(
        val text: String,
        val indentLevel: Int,
        val isOrdered: Boolean,
        val orderNumber: Int = 1,
        val isTask: Boolean = false,
        val isChecked: Boolean = false
    ) : MarkdownNode
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownNode
    object Divider : MarkdownNode
}

enum class AlertType(val label: String) {
    NOTE("Note"),
    TIP("Tip"),
    IMPORTANT("Important"),
    WARNING("Warning"),
    CAUTION("Caution")
}

// ── Markdown AST Compiler & Parser ──────────────────────────────────────────

object MarkdownCompiler {

    fun parse(markdown: String): List<MarkdownNode> {
        val nodes = mutableListOf<MarkdownNode>()
        val lines = markdown.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            when {
                // 1. Fenced Code Block: ```lang
                trimmed.startsWith("```") -> {
                    val lang = trimmed.removePrefix("```").trim()
                    val codeLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].trim().startsWith("```")) {
                        codeLines.add(lines[i])
                        i++
                    }
                    if (i < lines.size && lines[i].trim().startsWith("```")) {
                        i++ // consume closing ```
                    }
                    nodes.add(MarkdownNode.CodeBlock(language = lang, code = codeLines.joinToString("\n")))
                }

                // 2. Horizontal Divider: ---, ***, ___
                trimmed == "---" || trimmed == "***" || trimmed == "___" ||
                (trimmed.length >= 3 && trimmed.all { it == '-' }) ||
                (trimmed.length >= 3 && trimmed.all { it == '*' }) -> {
                    nodes.add(MarkdownNode.Divider)
                    i++
                }

                // 3. Headings: # H1 through ###### H6
                trimmed.startsWith("#") && trimmed.contains(" ") -> {
                    val hashCount = trimmed.takeWhile { it == '#' }.length
                    if (hashCount in 1..6 && trimmed.length > hashCount && trimmed[hashCount] == ' ') {
                        val headerText = trimmed.drop(hashCount + 1).trim()
                        nodes.add(MarkdownNode.Header(level = hashCount, text = headerText))
                        i++
                    } else {
                        nodes.add(MarkdownNode.Paragraph(line))
                        i++
                    }
                }

                // 4. Blockquotes & GitHub-style Alerts: > text
                trimmed.startsWith(">") -> {
                    val quoteLines = mutableListOf<String>()
                    var alertType: AlertType? = null

                    while (i < lines.size && lines[i].trim().startsWith(">")) {
                        val rawContent = lines[i].trim().removePrefix(">").trim()
                        if (quoteLines.isEmpty()) {
                            // Check for [!NOTE], [!TIP], [!IMPORTANT], [!WARNING], [!CAUTION]
                            val match = Regex("""^\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\]""", RegexOption.IGNORE_CASE).find(rawContent)
                            if (match != null) {
                                alertType = when (match.groupValues[1].uppercase()) {
                                    "NOTE" -> AlertType.NOTE
                                    "TIP" -> AlertType.TIP
                                    "IMPORTANT" -> AlertType.IMPORTANT
                                    "WARNING" -> AlertType.WARNING
                                    "CAUTION" -> AlertType.CAUTION
                                    else -> null
                                }
                                val rest = rawContent.substring(match.range.last + 1).trim()
                                if (rest.isNotBlank()) quoteLines.add(rest)
                            } else {
                                quoteLines.add(rawContent)
                            }
                        } else {
                            quoteLines.add(rawContent)
                        }
                        i++
                    }
                    nodes.add(MarkdownNode.BlockQuote(alertType = alertType, text = quoteLines.joinToString("\n")))
                }

                // 5. Tables: | Header | Header |
                trimmed.startsWith("|") && trimmed.endsWith("|") && i + 1 < lines.size && isTableDivider(lines[i + 1].trim()) -> {
                    val headers = parseTableRow(trimmed)
                    i += 2 // skip header and separator
                    val rows = mutableListOf<List<String>>()
                    while (i < lines.size && lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) {
                        rows.add(parseTableRow(lines[i].trim()))
                        i++
                    }
                    nodes.add(MarkdownNode.Table(headers = headers, rows = rows))
                }

                // 6. Task List Items: - [ ] or - [x] or * [ ] or * [x]
                isTaskListItem(line) -> {
                    val indent = (line.length - line.trimStart().length) / 2
                    val trimmedStart = line.trimStart()
                    val isChecked = trimmedStart.startsWith("- [x]", ignoreCase = true) ||
                                    trimmedStart.startsWith("* [x]", ignoreCase = true)
                    val content = trimmedStart.drop(5).trim()
                    nodes.add(
                        MarkdownNode.ListItem(
                            text = content,
                            indentLevel = indent,
                            isOrdered = false,
                            isTask = true,
                            isChecked = isChecked
                        )
                    )
                    i++
                }

                // 7. Unordered List Items: - item, * item, + item
                isUnorderedListItem(line) -> {
                    val indent = (line.length - line.trimStart().length) / 2
                    val content = line.trimStart().drop(2).trim()
                    nodes.add(
                        MarkdownNode.ListItem(
                            text = content,
                            indentLevel = indent,
                            isOrdered = false
                        )
                    )
                    i++
                }

                // 8. Ordered List Items: 1. item, 2. item
                isOrderedListItem(line) -> {
                    val indent = (line.length - line.trimStart().length) / 2
                    val trimmedStart = line.trimStart()
                    val dotIdx = trimmedStart.indexOf('.')
                    val num = trimmedStart.substring(0, dotIdx).toIntOrNull() ?: 1
                    val content = trimmedStart.substring(dotIdx + 1).trim()
                    nodes.add(
                        MarkdownNode.ListItem(
                            text = content,
                            indentLevel = indent,
                            isOrdered = true,
                            orderNumber = num
                        )
                    )
                    i++
                }

                // 9. Blank line: spacing
                trimmed.isBlank() -> {
                    i++
                }

                // 10. Normal Paragraph
                else -> {
                    val paragraphLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].isNotBlank() &&
                        !lines[i].trim().startsWith("#") &&
                        !lines[i].trim().startsWith("```") &&
                        !lines[i].trim().startsWith(">") &&
                        !lines[i].trim().startsWith("---") &&
                        !isUnorderedListItem(lines[i]) &&
                        !isOrderedListItem(lines[i]) &&
                        !isTaskListItem(lines[i]) &&
                        !(lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|"))
                    ) {
                        paragraphLines.add(lines[i].trim())
                        i++
                    }
                    if (paragraphLines.isNotEmpty()) {
                        nodes.add(MarkdownNode.Paragraph(paragraphLines.joinToString(" ")))
                    }
                }
            }
        }

        return nodes
    }

    private fun isUnorderedListItem(line: String): Boolean {
        val trimmed = line.trimStart()
        return (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ")) &&
               !trimmed.startsWith("- [ ]") && !trimmed.startsWith("- [x]") &&
               !trimmed.startsWith("* [ ]") && !trimmed.startsWith("* [x]")
    }

    private fun isTaskListItem(line: String): Boolean {
        val trimmed = line.trimStart()
        return trimmed.startsWith("- [ ] ") || trimmed.startsWith("- [x] ", ignoreCase = true) ||
               trimmed.startsWith("* [ ] ") || trimmed.startsWith("* [x] ", ignoreCase = true)
    }

    private fun isOrderedListItem(line: String): Boolean {
        val trimmed = line.trimStart()
        val match = Regex("""^\d+\.\s+""").find(trimmed)
        return match != null
    }

    private fun isTableDivider(line: String): Boolean {
        if (!line.startsWith("|") || !line.endsWith("|")) return false
        val cells = line.split("|").filter { it.isNotBlank() }
        return cells.isNotEmpty() && cells.all { cell ->
            val c = cell.trim()
            c.all { it == '-' || it == ':' }
        }
    }

    private fun parseTableRow(line: String): List<String> {
        return line.trim()
            .removePrefix("|")
            .removeSuffix("|")
            .split("|")
            .map { it.trim() }
    }
}

// ── Inline Markdown Spans (Links, Bold, Italic, Strikethrough, Code) ─────────

@Composable
fun rememberInlineMarkdown(
    text: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    linkColor: Color = MaterialTheme.colorScheme.primary
): AnnotatedString {
    val codeBg = MaterialTheme.colorScheme.surfaceContainerHighest
    return remember(text, textColor, linkColor, codeBg) {
        compileInlineMarkdown(text, textColor, linkColor, codeBg)
    }
}

fun compileInlineMarkdown(
    text: String,
    textColor: Color,
    linkColor: Color,
    codeBg: Color
): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // Link: [label](url)
                text.startsWith("[", i) -> {
                    val closeBracket = text.indexOf("]", i + 1)
                    if (closeBracket != -1 && closeBracket + 1 < text.length && text[closeBracket + 1] == '(') {
                        val closeParen = text.indexOf(")", closeBracket + 2)
                        if (closeParen != -1) {
                            val label = text.substring(i + 1, closeBracket)
                            val url = text.substring(closeBracket + 2, closeParen).trim()
                            val linkAnnotation = LinkAnnotation.Url(
                                url = url,
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = linkColor,
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = TextDecoration.Underline
                                    )
                                )
                            )
                            pushLink(linkAnnotation)
                            append(label)
                            pop()
                            i = closeParen + 1
                            continue
                        }
                    }
                    append(text[i])
                    i++
                }

                // Autolink bare URLs: http:// or https://
                (text.startsWith("http://", i) || text.startsWith("https://", i)) -> {
                    val endIdx = text.indexOfAny(charArrayOf(' ', '\t', '\n', ')', ']', '>', '"', '\''), i)
                    val url = if (endIdx == -1) text.substring(i) else text.substring(i, endIdx)
                    val linkAnnotation = LinkAnnotation.Url(
                        url = url,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                    pushLink(linkAnnotation)
                    append(url)
                    pop()
                    i += url.length
                }

                // Bold + Italic: ***text*** or ___text___
                text.startsWith("***", i) || text.startsWith("___", i) -> {
                    val delim = text.substring(i, i + 3)
                    val end = text.indexOf(delim, i + 3)
                    if (end != -1) {
                        val content = text.substring(i + 3, end)
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic))
                        append(content)
                        pop()
                        i = end + 3
                    } else {
                        append(text[i])
                        i++
                    }
                }

                // Bold: **text** or __text__
                text.startsWith("**", i) || text.startsWith("__", i) -> {
                    val delim = text.substring(i, i + 2)
                    val end = text.indexOf(delim, i + 2)
                    if (end != -1) {
                        val content = text.substring(i + 2, end)
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        append(content)
                        pop()
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }

                // Strikethrough: ~~text~~
                text.startsWith("~~", i) -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end != -1) {
                        val content = text.substring(i + 2, end)
                        pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                        append(content)
                        pop()
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }

                // Inline code: `code`
                text.startsWith("`", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        val content = text.substring(i + 1, end)
                        pushStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = codeBg,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                        append(" $content ")
                        pop()
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                // Italic: *text* or _text_
                text.startsWith("*", i) || text.startsWith("_", i) -> {
                    val delim = text[i]
                    val end = text.indexOf(delim, i + 1)
                    if (end != -1 && end > i + 1) {
                        val content = text.substring(i + 1, end)
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(content)
                        pop()
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }

                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

// ── Complete Composable Markdown Compiler Component ─────────────────────────

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val nodes = remember(markdown) { MarkdownCompiler.parse(markdown) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        nodes.forEach { node ->
            when (node) {
                is MarkdownNode.Header -> {
                    val (style, topPad) = when (node.level) {
                        1 -> MaterialTheme.typography.titleLarge to 12.dp
                        2 -> MaterialTheme.typography.titleMedium to 10.dp
                        3 -> MaterialTheme.typography.titleSmall to 8.dp
                        4 -> MaterialTheme.typography.bodyLarge to 6.dp
                        else -> MaterialTheme.typography.bodyMedium to 4.dp
                    }
                    Text(
                        text = rememberInlineMarkdown(node.text, textColor),
                        style = style,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = topPad, bottom = 2.dp)
                    )
                }

                is MarkdownNode.Paragraph -> {
                    Text(
                        text = rememberInlineMarkdown(node.text, textColor),
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor,
                        lineHeight = 20.sp
                    )
                }

                is MarkdownNode.ListItem -> {
                    val startPadding = (node.indentLevel * 14 + 2).dp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = startPadding, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        if (node.isTask) {
                            Icon(
                                imageVector = if (node.isChecked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                contentDescription = null,
                                tint = if (node.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        } else if (node.isOrdered) {
                            Text(
                                text = "${node.orderNumber}. ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "• ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = rememberInlineMarkdown(node.text, textColor),
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor,
                            lineHeight = 19.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownNode.CodeBlock -> {
                    MarkdownCodeBlockView(
                        language = node.language,
                        code = node.code
                    )
                }

                is MarkdownNode.BlockQuote -> {
                    MarkdownBlockQuoteView(
                        alertType = node.alertType,
                        text = node.text,
                        defaultTextColor = textColor
                    )
                }

                is MarkdownNode.Table -> {
                    MarkdownTableView(table = node)
                }

                is MarkdownNode.Divider -> {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

// ── Code Block View with Syntax Bar & Copy Action ────────────────────────────

@Composable
fun MarkdownCodeBlockView(
    language: String,
    code: String,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.ifBlank { "code" }.lowercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(code))
                        copied = true
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Filled.Check else Icons.Outlined.ContentCopy,
                        contentDescription = "Copy code",
                        tint = if (copied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Code Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(10.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ── BlockQuote & GitHub Alert Callout View ───────────────────────────────────

@Composable
fun MarkdownBlockQuoteView(
    alertType: AlertType?,
    text: String,
    defaultTextColor: Color,
    modifier: Modifier = Modifier
) {
    val (accentColor, icon: ImageVector?) = when (alertType) {
        AlertType.NOTE -> MaterialTheme.colorScheme.primary to Icons.Outlined.Info
        AlertType.TIP -> Color(0xFF10B981) to Icons.Outlined.Lightbulb
        AlertType.IMPORTANT -> Color(0xFF8B5CF6) to Icons.Outlined.PriorityHigh
        AlertType.WARNING -> Color(0xFFF59E0B) to Icons.Outlined.Warning
        AlertType.CAUTION -> MaterialTheme.colorScheme.error to Icons.Outlined.ErrorOutline
        null -> MaterialTheme.colorScheme.outlineVariant to null
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = accentColor.copy(alpha = 0.08f),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Left Accent Bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(if (alertType != null) 36.dp else 24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (alertType != null && icon != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = alertType.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
                Text(
                    text = rememberInlineMarkdown(text, defaultTextColor),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = if (alertType == null) FontStyle.Italic else FontStyle.Normal,
                    color = defaultTextColor.copy(alpha = 0.9f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ── Markdown Table View ──────────────────────────────────────────────────────

@Composable
fun MarkdownTableView(
    table: MarkdownNode.Table,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Headers
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    table.headers.forEach { header ->
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = header,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Rows
                table.rows.forEachIndexed { idx, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        row.forEachIndexed { colIdx, cell ->
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = cell,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    if (idx < table.rows.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
