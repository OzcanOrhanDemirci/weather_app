package com.ozcanorhandemirci.hava.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaSpacing
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.model.DailyPoint
import com.ozcanorhandemirci.hava.core.ui.describe
import com.ozcanorhandemirci.hava.core.ui.format
import androidx.compose.ui.text.intl.Locale
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale as JavaLocale

/**
 * The week ahead.
 *
 * Each day is a bar laid on the range of the whole week rather than on its own
 * range, so a cold Thursday sits visibly to the left of a mild Friday. Drawing
 * each bar full width would make every day look the same, which is the opposite
 * of what a week of weather is for.
 */
@Composable
internal fun DailyOutlook(days: List<DailyPoint>, modifier: Modifier = Modifier) {
    if (days.isEmpty()) return

    val palette = HavaTheme.sky
    val coldest = days.minOf { it.minimum.celsius }.toFloat()
    val warmest = days.maxOf { it.maximum.celsius }.toFloat()
    val span = (warmest - coldest).takeIf { it > 0.5f } ?: 1f
    // Read through the composition rather than from the platform directly, so
    // that changing the language of the device renames the days without a
    // restart.
    val languageTag = Locale.current.toLanguageTag()
    val locale = remember(languageTag) { JavaLocale.forLanguageTag(languageTag) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HavaSpacing.compact),
    ) {
        days.forEachIndexed { index, day ->
            val dayName = if (index == 0) {
                null
            } else {
                day.date.dayOfWeek.getDisplayName(DateTextStyle.SHORT, locale)
            }
            DayRow(
                day = day,
                label = dayName,
                coldest = coldest,
                span = span,
                barColor = palette.accent,
                trackColor = palette.glassEdge,
            )
        }
    }
}

@Composable
private fun DayRow(
    day: DailyPoint,
    label: String?,
    coldest: Float,
    span: Float,
    barColor: androidx.compose.ui.graphics.Color,
    trackColor: androidx.compose.ui.graphics.Color,
) {
    val palette = HavaTheme.sky
    val today = androidx.compose.ui.res.stringResource(R.string.detail_today)
    val name = label ?: today
    val spoken = "$name, ${day.kind.describe()}, ${day.minimum.format()} ${day.maximum.format()}"

    Row(
        modifier = Modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = spoken },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HavaSpacing.compact),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = palette.content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(DAY_NAME_WIDTH),
        )

        Text(
            text = day.minimum.format(),
            style = HavaTheme.typography.metric,
            color = palette.contentMuted,
            modifier = Modifier.width(READING_WIDTH),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(BAR_HEIGHT)
                .clip(MaterialTheme.shapes.extraSmall)
                .drawBehind {
                    drawRoundRect(
                        color = trackColor,
                        cornerRadius = CornerRadius(size.height / 2f),
                    )

                    val start = ((day.minimum.celsius.toFloat() - coldest) / span) * size.width
                    val end = ((day.maximum.celsius.toFloat() - coldest) / span) * size.width

                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(barColor.copy(alpha = 0.55f), barColor),
                            startX = start,
                            endX = end,
                        ),
                        topLeft = Offset(start, 0f),
                        size = Size(width = (end - start).coerceAtLeast(size.height), height = size.height),
                        cornerRadius = CornerRadius(size.height / 2f),
                    )
                },
        )

        Text(
            text = day.maximum.format(),
            style = HavaTheme.typography.metric,
            color = palette.content,
            modifier = Modifier.width(READING_WIDTH),
        )
    }
}

private val DAY_NAME_WIDTH = 56.dp
private val READING_WIDTH = 40.dp
private val BAR_HEIGHT = 6.dp
