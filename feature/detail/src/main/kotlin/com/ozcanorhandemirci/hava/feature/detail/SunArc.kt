package com.ozcanorhandemirci.hava.feature.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaSpacing
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.sin

/**
 * The path of the sun between this morning and this evening, with where it has
 * reached.
 *
 * The arc is the shape of the day: short and low in December, long and high in
 * June. Sunrise and sunset as two pieces of text say the same thing, and say it
 * in a way nobody pictures.
 */
@Composable
internal fun SunArc(
    sunrise: Instant,
    sunset: Instant,
    now: Instant,
    zone: ZoneId,
    modifier: Modifier = Modifier,
) {
    val palette = HavaTheme.sky
    val format = remember(zone) { DateTimeFormatter.ofPattern("HH:mm").withZone(zone) }

    val length = (sunset.epochSecond - sunrise.epochSecond).toFloat()
    val progress = if (length <= 0f) {
        0f
    } else {
        ((now.epochSecond - sunrise.epochSecond) / length).coerceIn(0f, 1f)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HavaSpacing.small),
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(ARC_HEIGHT)) {
            val arc = Path().apply {
                moveTo(0f, size.height)
                cubicTo(
                    size.width * 0.25f, size.height * ARC_LIFT,
                    size.width * 0.75f, size.height * ARC_LIFT,
                    size.width, size.height,
                )
            }

            drawPath(
                path = arc,
                color = palette.contentMuted.copy(alpha = TRACK_ALPHA),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f)),
                ),
            )

            drawLine(
                color = palette.contentMuted.copy(alpha = HORIZON_ALPHA),
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 1.dp.toPx(),
            )

            // Height along the arc follows a sine, which is how the sun moves:
            // quickly near the horizon and slowly near the top.
            val x = progress * size.width
            val y = size.height - sin(progress * PI).toFloat() * size.height * (1f - ARC_LIFT)

            drawCircle(
                color = palette.luminaryGlow,
                radius = SUN_RADIUS.toPx() * GLOW_SCALE,
                center = Offset(x, y),
            )
            drawCircle(color = palette.luminary, radius = SUN_RADIUS.toPx(), center = Offset(x, y))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Labelled(stringResource(R.string.detail_sunrise), format.format(sunrise))
            Labelled(stringResource(R.string.detail_sunset), format.format(sunset))
        }
    }
}

@Composable
private fun Labelled(label: String, value: String) {
    val palette = HavaTheme.sky
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, style = HavaTheme.typography.overline, color = palette.contentMuted)
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = palette.content)
    }
}

private val ARC_HEIGHT = 96.dp
private val SUN_RADIUS = 6.dp

/** How far up the control points pull the arc, as a share of the height. */
private const val ARC_LIFT = 0.1f
private const val GLOW_SCALE = 2.6f
private const val TRACK_ALPHA = 0.55f
private const val HORIZON_ALPHA = 0.3f
