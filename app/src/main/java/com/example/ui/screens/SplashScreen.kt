package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalAppPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val APP_NAME = "Ustaca"

/**
 * Açılış: logo kartı yaylanarak gelir, arkasında yeşil halka yayılır,
 * "Ustaca" harf harf belirir, slogan ve ilerleme çizgisi tamamlanınca uygulama açılır (~1.9 sn).
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val palette = LocalAppPalette.current

    val logoScale = remember { Animatable(0.55f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoRotation = remember { Animatable(-12f) }
    val ring = remember { Animatable(0f) }
    val letters = remember { List(APP_NAME.length) { Animatable(0f) } }
    val tagline = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { logoAlpha.animateTo(1f, tween(280)) }
        launch {
            logoScale.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
        }
        launch { logoRotation.animateTo(0f, tween(650, easing = FastOutSlowInEasing)) }
        launch { ring.animateTo(1f, tween(1100, easing = FastOutSlowInEasing)) }
        launch { progress.animateTo(1f, tween(1750, easing = LinearEasing)) }

        delay(380)
        letters.forEachIndexed { index, anim ->
            launch {
                delay(index * 70L)
                anim.animateTo(1f, tween(320, easing = FastOutSlowInEasing))
            }
        }
        delay(APP_NAME.length * 70L + 200L)
        tagline.animateTo(1f, tween(380))
        delay(650)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                // Yayılan halkalar
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = ring.value
                    val maxRadius = size.minDimension / 2f
                    drawCircle(
                        color = palette.accent.copy(alpha = 0.10f * (1f - r) + 0.04f),
                        radius = maxRadius * (0.45f + 0.55f * r)
                    )
                    drawCircle(
                        color = palette.ink.copy(alpha = 0.35f * (1f - r)),
                        radius = maxRadius * (0.40f + 0.60f * r),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Logo kartı
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = logoScale.value
                            scaleY = logoScale.value
                            rotationZ = logoRotation.value
                            alpha = logoAlpha.value
                        }
                        .size(108.dp)
                        .shadow(elevation = 14.dp, shape = RoundedCornerShape(30.dp), clip = false)
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, palette.cardBorder, RoundedCornerShape(30.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_brand_logo),
                        contentDescription = "Ustaca logosu",
                        modifier = Modifier.size(70.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                APP_NAME.forEachIndexed { index, ch ->
                    val v = letters[index].value
                    Text(
                        text = ch.toString(),
                        color = if (index == 0) palette.ink else MaterialTheme.colorScheme.onBackground,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.graphicsLayer {
                            alpha = v
                            translationY = (1f - v) * 18.dp.toPx()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "İşini ustaca yönet.",
                color = palette.muted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.graphicsLayer {
                    alpha = tagline.value
                    translationY = (1f - tagline.value) * 8.dp.toPx()
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // İlerleme çizgisi
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.accentSoft)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.value)
                        .clip(RoundedCornerShape(2.dp))
                        .background(palette.ink)
                )
            }
        }

        Text(
            text = "EA Vision Labs",
            color = palette.inactive,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .graphicsLayer { alpha = tagline.value }
        )
    }
}
