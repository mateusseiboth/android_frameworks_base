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

package com.android.systemui.statusbar.notification.stack

import org.junit.Assert.assertEquals
import org.junit.Test

class ExpandedRowScrollTest {

    @Test
    fun rowInsideTheVisibleStack_doesNotScroll() {
        assertEquals(0f, scrollToRevealExpandedRow(rowTop = 100f, rowBottom = 500f), DELTA)
    }

    @Test
    fun rowEndingExactlyAtTheBottom_doesNotScroll() {
        assertEquals(0f, scrollToRevealExpandedRow(rowTop = 600f, rowBottom = 1000f), DELTA)
    }

    @Test
    fun rowPassingTheBottom_scrollsByWhatPassesIt() {
        assertEquals(150f, scrollToRevealExpandedRow(rowTop = 700f, rowBottom = 1150f), DELTA)
    }

    @Test
    fun rowTallerThanTheStack_scrollsUntilItsTopReachesTheTop() {
        assertEquals(800f, scrollToRevealExpandedRow(rowTop = 900f, rowBottom = 2500f), DELTA)
    }

    @Test
    fun rowWithItsTopAboveTheStack_doesNotScroll() {
        assertEquals(0f, scrollToRevealExpandedRow(rowTop = 50f, rowBottom = 1200f), DELTA)
    }

    private fun scrollToRevealExpandedRow(rowTop: Float, rowBottom: Float) =
        scrollToRevealExpandedRow(
            rowTop = rowTop,
            rowBottom = rowBottom,
            visibleTop = VISIBLE_TOP,
            visibleBottom = VISIBLE_BOTTOM,
        )

    private companion object {
        const val VISIBLE_TOP = 100f
        const val VISIBLE_BOTTOM = 1000f
        const val DELTA = 0.001f
    }
}
