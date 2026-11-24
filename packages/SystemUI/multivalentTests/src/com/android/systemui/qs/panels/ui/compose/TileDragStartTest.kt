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

package com.android.systemui.qs.panels.ui.compose

import org.junit.Assert.assertEquals
import org.junit.Test

class TileDragStartTest {

    @Test
    fun find_mouseWithImmediateDrag_startsAfterTouchSlop() {
        assertEquals(
            TileDragStart.AfterTouchSlop,
            TileDragStart.find(isMouse = true, isImmediateMouseDragEnabled = true),
        )
    }

    @Test
    fun find_mouseWithoutImmediateDrag_waitsForLongPress() {
        assertEquals(
            TileDragStart.AfterLongPress,
            TileDragStart.find(isMouse = true, isImmediateMouseDragEnabled = false),
        )
    }

    @Test
    fun find_touchWithImmediateDrag_waitsForLongPress() {
        assertEquals(
            TileDragStart.AfterLongPress,
            TileDragStart.find(isMouse = false, isImmediateMouseDragEnabled = true),
        )
    }

    @Test
    fun find_touchWithoutImmediateDrag_waitsForLongPress() {
        assertEquals(
            TileDragStart.AfterLongPress,
            TileDragStart.find(isMouse = false, isImmediateMouseDragEnabled = false),
        )
    }
}
