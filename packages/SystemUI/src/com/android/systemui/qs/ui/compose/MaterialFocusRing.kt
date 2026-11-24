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

package com.android.systemui.qs.ui.compose

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.android.compose.modifiers.thenIf
import com.android.systemui.res.R

/** Where a focus ring goes relative to the focused element. */
enum class FocusRingPlacement {
    /** Around the element, at the Material 3 gap. */
    Outside,
    /** Along the element's inner edge, for elements whose parent clips anything outside them. */
    Inside;

    fun padding(strokeWidth: Dp, gap: Dp): Dp =
        when (this) {
            Outside -> gap
            Inside -> -strokeWidth / 2
        }
}

/**
 * A Material 3 keyboard focus ring with the color, stroke width and gap shared by all SystemUI
 * surfaces, shown when config_materialFocusRings is set. Place it before the clickable it rings.
 */
@Composable
fun Modifier.materialFocusRing(
    cornerSize: CornerSize,
    placement: FocusRingPlacement = FocusRingPlacement.Outside,
): Modifier {
    val isEnabled = booleanResource(R.bool.config_materialFocusRings)
    val strokeWidth = dimensionResource(R.dimen.material_focus_ring_stroke_width)
    val padding = placement.padding(strokeWidth, dimensionResource(R.dimen.material_focus_ring_gap))
    val color = MaterialTheme.colorScheme.secondary
    return thenIf(isEnabled) {
        Modifier.borderOnFocus(
            color = color,
            cornerSize = cornerSize,
            strokeWidth = strokeWidth,
            paddingHorizontal = padding,
            paddingVertical = padding,
        )
    }
}
