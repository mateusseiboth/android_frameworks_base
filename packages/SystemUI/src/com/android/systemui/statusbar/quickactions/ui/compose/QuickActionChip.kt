/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.quickactions.ui.compose

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.compose.modifiers.thenIf
import com.android.systemui.common.shared.model.ContentDescription
import com.android.systemui.common.ui.compose.Icon
import com.android.systemui.common.ui.compose.load
import com.android.systemui.res.R
import com.android.systemui.statusbar.chips.ui.viewmodel.rememberChronometerState
import com.android.systemui.statusbar.quickactions.shared.model.ChipContent
import com.android.systemui.statusbar.quickactions.shared.model.ChipIcon

/**
 * A clickable chip that can show an anchored popup containing relevant system controls. The chip
 * can show an icon that can have its own separate action distinct from its parent chip. Moreover,
 * the chip can show text containing contextual information.
 */
@Composable
fun QuickActionChip(
    isSelected: Boolean,
    chipContent: ChipContent?,
    icons: List<ChipIcon>,
    colors: ChipColors,
    contentDescription: ContentDescription?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hoverIcons: List<ChipIcon>? = null,
    isTitleChip: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    cornerRadius: Dp = dimensionResource(id = R.dimen.ongoing_activity_chip_corner_radius),
    horizontalPadding: PaddingValues =
        PaddingValues(
            start = 4.dp,
            end =
                if (
                    (chipContent is ChipContent.Text || chipContent is ChipContent.Timer) &&
                        icons.isNotEmpty()
                )
                    8.dp
                else 4.dp,
        ),
) {
    val hoveredState by interactionSource.collectIsHoveredAsState()
    val indication = if (hoveredState) null else LocalIndication.current
    val chipShape = RoundedCornerShape(cornerRadius)
    val chipBackgroundColor =
        colors.chipBackground(isSelected = isSelected, colorScheme = MaterialTheme.colorScheme)

    // Use a Box with `fillMaxHeight` to create a larger click surface for the chip. The visible
    // height of the chip is determined by the height of the background of the Row below. The
    // `indication` for Clicks is applied in the Row below as well.
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .contentDescription(contentDescription)
                .clickable(
                    onClick = onClick,
                    indication = null,
                    interactionSource = interactionSource,
                ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                chipWidth(isTitleChip)
                    .height(dimensionResource(R.dimen.ongoing_appops_chip_height))
                    .clip(chipShape)
                    .background(chipBackgroundColor)
                    .border(
                        width = dimensionResource(id = R.dimen.ongoing_activity_chip_outline_width),
                        color =
                            colors.chipOutline(
                                isSelected = isSelected,
                                colorScheme = MaterialTheme.colorScheme,
                            ),
                        shape = chipShape,
                    )
                    .indication(interactionSource, indication)
                    .padding(horizontalPadding),
        ) {
            ChipIcons(
                chipIcons = chipIconsFor(icons, hoverIcons, hoveredState),
                colors = colors,
                isSelected = isSelected,
                iconSize = chipIconSize(isTitleChip),
            )

            when (chipContent) {
                is ChipContent.Text ->
                    when {
                        isTitleChip ->
                            TitleChipText(
                                text = chipContent.text,
                                colors = colors,
                                isSelected = isSelected,
                                isHovered = hoveredState,
                            )
                        else ->
                            ChipText(
                                text = chipContent.text,
                                colors = colors,
                                isSelected = isSelected,
                            )
                    }
                is ChipContent.Timer -> {
                    val timerState =
                        rememberChronometerState(
                            chronometer = chipContent.chronometer,
                            timeSource = chipContent.timeSource,
                        )
                    timerState.currentTimeText?.let {
                        ChipText(
                            text = it,
                            colors = colors,
                            isSelected = isSelected,
                            textStyle =
                                MaterialTheme.typography.labelLarge.copy(
                                    fontFeatureSettings = "tnum"
                                ),
                        )
                    }
                }
                is ChipContent.IconOnly -> {
                    Icon(
                        icon = chipContent.icon,
                        modifier = Modifier.size(13.3.dp).clickable { onClick() },
                        tint =
                            colors.chipContent(
                                isSelected = isSelected,
                                colorScheme = MaterialTheme.colorScheme,
                            ),
                    )
                }
                null -> {}
            }
        }
    }
}

/** A title chip keeps its width between a minimum and a maximum; other chips wrap content. */
@Composable
private fun chipWidth(isTitleChip: Boolean): Modifier {
    if (!isTitleChip) return Modifier
    return Modifier.widthIn(
        min = dimensionResource(R.dimen.status_bar_media_chip_min_width),
        max = dimensionResource(R.dimen.status_bar_media_chip_max_width),
    )
}

@Composable
private fun chipIconSize(isTitleChip: Boolean): Dp {
    if (!isTitleChip) return 20.dp
    return dimensionResource(R.dimen.status_bar_media_chip_icon_size)
}

/**
 * The text of a title chip: it takes the width the chip has left, fades at its end when it does
 * not fit, and scrolls while the pointer is over the chip so the whole title can be read.
 */
@Composable
private fun TitleChipText(
    text: String,
    colors: ChipColors,
    isSelected: Boolean,
    isHovered: Boolean,
) {
    val textStyle = MaterialTheme.typography.titleSmallEmphasized
    val textMeasurer = rememberTextMeasurer()
    var textOverflow by remember { mutableStateOf(false) }
    val overflowStyle =
        chipTextOverflowStyle(isTitleChip = true, isHovered = isHovered, hasOverflow = textOverflow)

    Text(
        text = text,
        style = textStyle,
        softWrap = false,
        color =
            colors.chipContent(isSelected = isSelected, colorScheme = MaterialTheme.colorScheme),
        modifier =
            Modifier.measureOverflow(text, textStyle, textMeasurer) { textOverflow = it }
                .fadeEdges(
                    fadeEdges = { overflowStyle.fadeEdges },
                    fadeLength =
                        dimensionResource(R.dimen.status_bar_media_chip_text_fading_edge_length),
                )
                .thenIf(overflowStyle.isScrolling) {
                    Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                },
    )
}

/**
 * Reports whether [text] is wider than the space it gets, and draws it in an offscreen layer
 * when it is, which the fade of [fadeEdges] needs.
 */
private fun Modifier.measureOverflow(
    text: String,
    textStyle: TextStyle,
    textMeasurer: TextMeasurer,
    onOverflowChanged: (Boolean) -> Unit,
): Modifier = layout { measurables, constraints ->
    val placeable = measurables.measure(constraints)
    val intrinsicWidth = textMeasurer.measure(text, textStyle, softWrap = false).size.width
    val hasOverflow = intrinsicWidth > constraints.maxWidth
    onOverflowChanged(hasOverflow)

    layout(placeable.width, placeable.height) {
        if (!hasOverflow) return@layout placeable.place(0, 0)
        placeable.placeWithLayer(0, 0) { compositingStrategy = CompositingStrategy.Offscreen }
    }
}

@Composable
private fun ChipText(
    text: String,
    colors: ChipColors,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    val textMeasurer = rememberTextMeasurer()
    var textOverflow by remember { mutableStateOf(false) }

    Text(
        text = text,
        style = textStyle,
        softWrap = false,
        color =
            colors.chipContent(isSelected = isSelected, colorScheme = MaterialTheme.colorScheme),
        modifier =
            modifier
                .widthIn(max = dimensionResource(id = R.dimen.ongoing_activity_chip_max_text_width))
                .layout { measurables, constraints ->
                    val placeable = measurables.measure(constraints)
                    val intrinsicWidth =
                        textMeasurer.measure(text, textStyle, softWrap = false).size.width
                    textOverflow = intrinsicWidth > constraints.maxWidth

                    layout(placeable.width, placeable.height) {
                        if (textOverflow) {
                            placeable.placeWithLayer(0, 0) {
                                compositingStrategy = CompositingStrategy.Offscreen
                            }
                        } else {
                            placeable.place(0, 0)
                        }
                    }
                }
                .overflowFadeOut(
                    hasOverflow = { textOverflow },
                    fadeLength =
                        dimensionResource(
                            id = R.dimen.ongoing_activity_chip_text_fading_edge_length
                        ),
                ),
    )
}

@Composable
private fun ChipIcons(
    chipIcons: List<ChipIcon>,
    colors: ChipColors,
    isSelected: Boolean,
    iconSize: Dp = 20.dp,
) {
    val iconBackgroundColor =
        colors.iconBackground(isSelected = isSelected, colorScheme = MaterialTheme.colorScheme)

    for (chipIcon in chipIcons) {

        Icon(
            icon = chipIcon.icon,
            modifier =
                Modifier.size(iconSize)
                    .thenIf(chipIcon.onClick != null) {
                        Modifier.clickable(role = Role.Button, onClick = chipIcon.onClick!!)
                    }
                    .thenIf(chipIcon.isHighlighted && iconBackgroundColor != Color.Unspecified) {
                        Modifier.background(color = iconBackgroundColor, shape = CircleShape)
                            .padding(2.dp)
                    },
            tint =
                colors.icon(
                    isSelected = isSelected,
                    isHighlighted = chipIcon.isHighlighted,
                    colorScheme = MaterialTheme.colorScheme,
                ),
        )
    }
}

private fun Modifier.fadeEdges(
    fadeEdges: () -> ChipTextFadeEdges,
    fadeLength: Dp,
): Modifier = drawWithCache {
    val width = size.width
    val fade = (fadeLength.toPx() / width).coerceIn(0f, 0.5f)
    val endFade =
        Brush.horizontalGradient(
            0f to Color.Black,
            1f - fade to Color.Black,
            1f to Color.Transparent,
        )
    val bothFade =
        Brush.horizontalGradient(
            0f to Color.Transparent,
            fade to Color.Black,
            1f - fade to Color.Black,
            1f to Color.Transparent,
        )
    onDrawWithContent {
        drawContent()
        when (fadeEdges()) {
            ChipTextFadeEdges.NONE -> {}
            ChipTextFadeEdges.END -> drawRect(brush = endFade, blendMode = BlendMode.DstIn)
            ChipTextFadeEdges.BOTH -> drawRect(brush = bothFade, blendMode = BlendMode.DstIn)
        }
    }
}

private fun Modifier.overflowFadeOut(hasOverflow: () -> Boolean, fadeLength: Dp): Modifier {
    return drawWithCache {
        val width = size.width
        val start = (width - fadeLength.toPx()).coerceAtLeast(0f)
        val gradient =
            Brush.horizontalGradient(
                colors = listOf(Color.Black, Color.Transparent),
                startX = start,
                endX = width,
            )
        onDrawWithContent {
            drawContent()
            if (hasOverflow()) drawRect(brush = gradient, blendMode = BlendMode.DstIn)
        }
    }
}

@Composable
private fun Modifier.contentDescription(description: ContentDescription?): Modifier {
    val resolvedDescription = description?.load() ?: return this
    return this.semantics { contentDescription = resolvedDescription }
}
