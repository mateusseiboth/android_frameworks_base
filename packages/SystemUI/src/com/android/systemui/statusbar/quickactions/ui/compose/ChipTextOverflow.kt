/*
 * Copyright (C) 2026 The LineageOS Project
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

/** Which edges of a chip's text fade out. */
enum class ChipTextFadeEdges {
    NONE,
    END,
    BOTH,
}

/** How a chip draws text that may not fit: which edges fade and whether the text scrolls. */
data class ChipTextOverflowStyle(val fadeEdges: ChipTextFadeEdges, val isScrolling: Boolean)

private val FITS = ChipTextOverflowStyle(ChipTextFadeEdges.NONE, isScrolling = false)
private val CUT_AT_END = ChipTextOverflowStyle(ChipTextFadeEdges.END, isScrolling = false)
private val SCROLLING = ChipTextOverflowStyle(ChipTextFadeEdges.BOTH, isScrolling = true)

/**
 * Text that fits is drawn as is. Text that does not fit fades at its end, except on a title chip
 * under the pointer, where it scrolls and fades at both edges so the whole title can be read.
 */
fun chipTextOverflowStyle(
    isTitleChip: Boolean,
    isHovered: Boolean,
    hasOverflow: Boolean,
): ChipTextOverflowStyle {
    if (!hasOverflow) return FITS
    if (isTitleChip && isHovered) return SCROLLING
    return CUT_AT_END
}

/** The icons a chip shows: its hover icons while the pointer is over it, if it has any. */
fun <T> chipIconsFor(icons: List<T>, hoverIcons: List<T>?, isHovered: Boolean): List<T> =
    hoverIcons?.takeIf { isHovered } ?: icons
