package com.ozcanorhandemirci.hava.feature.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import com.ozcanorhandemirci.hava.core.designsystem.theme.SkyPalette
import com.ozcanorhandemirci.hava.core.model.HourlyPoint
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * The next day, hour by hour, as a curve that can be dragged.
 *
 * This is the one control in the application that changes the whole screen.
 * Moving along it moves the sky behind everything: the light, the height of the
 * sun, whether it is raining. The forecast stops being a table of numbers and
 * becomes a day that can be walked through.
 *
 * Every hour crossed is marked with a tick under the finger, so the movement
 * can be felt as discrete even though the line is continuous.
 */
@Composable
internal fun HourlyCurve(
    hours: List<HourlyPoint>,
    zone: ZoneId,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (hours.isEmpty()) return

    val palette = HavaTheme.sky
    val measurer = rememberTextMeasurer()
    val haptics = LocalHapticFeedback.current
    val hourFormat = remember(zone) { DateTimeFormatter.ofPattern("HH").withZone(zone) }

    val labelStyle = HavaTheme.typography.metric.copy(color = palette.contentMuted)
    val readingStyle = HavaTheme.typography.temperatureCompact.copy(color = palette.content)

    val selection by rememberUpdatedState(selectedIndex)
    val select by rememberUpdatedState(onSelect)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(CURVE_HEIGHT)
            .pointerInput(hours.size) {
                fun pick(x: Float) {
                    val index = ((x / size.width) * (hours.size - 1))
                        .roundToInt()
                        .coerceIn(0, hours.lastIndex)
                    if (index != selection) {
                        haptics.tickForHour()
                        select(index)
                    }
                }

                detectHorizontalDragGestures(
                    onDragEnd = { haptics.performHapticFeedback(HapticFeedbackType.GestureEnd) },
                ) { change, _ -> pick(change.position.x) }
            }
            .pointerInput(hours.size) {
                detectTapGestures { offset ->
                    val index = ((offset.x / size.width) * (hours.size - 1))
                        .roundToInt()
                        .coerceIn(0, hours.lastIndex)
                    haptics.tickForHour()
                    select(index)
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(CURVE_HEIGHT)) {
            val readings = hours.map { it.temperature.celsius.toFloat() }
            val lowest = readings.min()
            val highest = readings.max()
            val span = (highest - lowest).takeIf { it > 0.5f } ?: 1f

            val points = readings.mapIndexed { index, value ->
                val height = (value - lowest) / span
                Offset(
                    x = index.toFloat() / (hours.size - 1) * size.width,
                    y = (PLOT_TOP + (1f - height) * (PLOT_BOTTOM - PLOT_TOP)) * size.height,
                )
            }

            drawNightBands(hours, palette)
            drawRainfall(hours, palette)

            val curve = smoothPathThrough(points)
            drawPath(
                path = Path().apply {
                    addPath(curve)
                    lineTo(size.width, size.height * PLOT_BOTTOM)
                    lineTo(0f, size.height * PLOT_BOTTOM)
                    close()
                },
                brush = Brush.verticalGradient(
                    colors = listOf(
                        palette.accent.copy(alpha = FILL_TOP_ALPHA),
                        Color.Transparent,
                    ),
                    startY = size.height * PLOT_TOP,
                    endY = size.height * PLOT_BOTTOM,
                ),
            )
            drawPath(
                path = curve,
                color = palette.content,
                style = Stroke(width = CURVE_STROKE.toPx(), cap = StrokeCap.Round),
            )

            drawSelection(
                point = points[selection.coerceIn(points.indices)],
                palette = palette,
            )

            drawHourLabels(hours, measurer, labelStyle, hourFormat)
            drawReading(
                point = points[selection.coerceIn(points.indices)],
                text = "${hours[selection.coerceIn(hours.indices)].temperature.celsius.roundToInt()}\u00B0",
                measurer = measurer,
                style = readingStyle,
            )
        }
    }
}

private fun HapticFeedback.tickForHour() =
    performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)

/**
 * A curve through every point, built from cubic segments whose control points
 * follow the slope of the neighbours.
 *
 * Straight lines between hourly readings would put a corner at each hour, which
 * says the temperature changed direction exactly on the hour. It did not; the
 * reading is a sample, not an event.
 */
private fun smoothPathThrough(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path

    path.moveTo(points.first().x, points.first().y)
    for (index in 0 until points.lastIndex) {
        val current = points[index]
        val next = points[index + 1]
        val handle = (next.x - current.x) * SMOOTHING
        path.cubicTo(current.x + handle, current.y, next.x - handle, next.y, next.x, next.y)
    }
    return path
}

/** Hours after sunset are shaded, so the night is visible as a block rather than read off labels. */
private fun DrawScope.drawNightBands(hours: List<HourlyPoint>, palette: SkyPalette) {
    val step = size.width / (hours.size - 1)
    hours.forEachIndexed { index, hour ->
        if (hour.isDay) return@forEachIndexed
        drawRect(
            color = palette.zenith.copy(alpha = NIGHT_BAND_ALPHA),
            topLeft = Offset(x = (index - 0.5f) * step, y = 0f),
            size = Size(width = step, height = size.height * PLOT_BOTTOM),
        )
    }
}

/** The chance of rain, as a column under each hour. */
private fun DrawScope.drawRainfall(hours: List<HourlyPoint>, palette: SkyPalette) {
    val step = size.width / (hours.size - 1)
    val base = size.height * PLOT_BOTTOM

    hours.forEachIndexed { index, hour ->
        val chance = hour.precipitationProbabilityPercent / 100f
        if (chance <= 0.05f) return@forEachIndexed

        val height = chance * size.height * RAIN_COLUMN_HEIGHT
        drawRect(
            color = palette.accent.copy(alpha = RAIN_COLUMN_ALPHA),
            topLeft = Offset(x = index * step - step * 0.3f, y = base - height),
            size = Size(width = step * 0.6f, height = height),
        )
    }
}

private fun DrawScope.drawSelection(point: Offset, palette: SkyPalette) {
    drawLine(
        color = palette.content.copy(alpha = GUIDE_ALPHA),
        start = Offset(point.x, 0f),
        end = Offset(point.x, size.height * PLOT_BOTTOM),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f)),
    )
    drawCircle(color = palette.content, radius = 5.dp.toPx(), center = point)
    drawCircle(color = palette.zenith, radius = 2.5f.dp.toPx(), center = point)
}

private fun DrawScope.drawHourLabels(
    hours: List<HourlyPoint>,
    measurer: TextMeasurer,
    style: TextStyle,
    format: DateTimeFormatter,
) {
    val step = size.width / (hours.size - 1)
    hours.forEachIndexed { index, hour ->
        if (index % LABEL_EVERY != 0) return@forEachIndexed

        val text = format.format(hour.time)
        val layout = measurer.measure(text, style)
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                x = (index * step - layout.size.width / 2f).coerceIn(0f, size.width - layout.size.width),
                y = size.height - layout.size.height,
            ),
        )
    }
}

private fun DrawScope.drawReading(
    point: Offset,
    text: String,
    measurer: TextMeasurer,
    style: TextStyle,
) {
    val layout = measurer.measure(text, style)
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(
            x = (point.x - layout.size.width / 2f).coerceIn(0f, size.width - layout.size.width),
            y = (point.y - layout.size.height - READING_LIFT.toPx()).coerceAtLeast(0f),
        ),
    )
}

private val CURVE_HEIGHT = 168.dp
private val CURVE_STROKE = 2.dp
private val READING_LIFT = 10.dp

/** Fraction of the height the plot occupies, leaving room for labels below. */
private const val PLOT_TOP = 0.26f
private const val PLOT_BOTTOM = 0.82f

private const val SMOOTHING = 0.42f
private const val FILL_TOP_ALPHA = 0.38f
private const val NIGHT_BAND_ALPHA = 0.22f
private const val RAIN_COLUMN_HEIGHT = 0.18f
private const val RAIN_COLUMN_ALPHA = 0.45f
private const val GUIDE_ALPHA = 0.45f
private const val LABEL_EVERY = 3
