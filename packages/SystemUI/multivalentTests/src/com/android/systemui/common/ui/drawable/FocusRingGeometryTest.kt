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

package com.android.systemui.common.ui.drawable

import org.junit.Assert.assertEquals
import org.junit.Test

class FocusRingGeometryTest {

    @Test
    fun find_shapeFillingTheView_ringStaysInsideTheView() {
        val bounds =
            FocusRingGeometry.find(
                width = 100f,
                height = 48f,
                shape = FocusRingShape(cornerRadius = 4f),
                strokeWidth = 3f,
                gap = 2f,
            )

        assertEquals(FocusRingBounds(1.5f, 1.5f, 98.5f, 46.5f, radius = 7.5f), bounds)
    }

    @Test
    fun find_insetShape_ringFollowsTheShapeWithTheGap() {
        val bounds =
            FocusRingGeometry.find(
                width = 100f,
                height = 48f,
                shape = FocusRingShape(cornerRadius = 4f, insetTop = 8f, insetBottom = 8f),
                strokeWidth = 3f,
                gap = 2f,
            )

        assertEquals(FocusRingBounds(1.5f, 4.5f, 98.5f, 43.5f, radius = 7.5f), bounds)
    }

    @Test
    fun find_largeCornerRadius_clampedToAPill() {
        val bounds =
            FocusRingGeometry.find(
                width = 100f,
                height = 48f,
                shape = FocusRingShape(cornerRadius = 24f, insetTop = 8f, insetBottom = 8f),
                strokeWidth = 3f,
                gap = 2f,
            )

        assertEquals(19.5f, bounds.radius)
    }

    @Test
    fun find_fullyRounded_halfTheShortestSide() {
        val bounds =
            FocusRingGeometry.find(
                width = 48f,
                height = 48f,
                shape = FocusRingShape(cornerRadius = FocusRingShape.FULLY_ROUNDED),
                strokeWidth = 3f,
                gap = 2f,
            )

        assertEquals(22.5f, bounds.radius)
    }

    @Test
    fun find_viewSmallerThanTheStroke_noNegativeRadius() {
        val bounds =
            FocusRingGeometry.find(
                width = 2f,
                height = 2f,
                shape = FocusRingShape(cornerRadius = 4f),
                strokeWidth = 3f,
                gap = 2f,
            )

        assertEquals(0f, bounds.radius)
    }
}
