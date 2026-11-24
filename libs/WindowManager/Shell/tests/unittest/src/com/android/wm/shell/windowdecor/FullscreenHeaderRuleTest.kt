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
package com.android.wm.shell.windowdecor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for [FullscreenHeaderRule]. */
class FullscreenHeaderRuleTest {

    private val withSplitHeader = FullscreenHeaderRule(isSplitScreenHeaderEnabled = true)
    private val withoutSplitHeader = FullscreenHeaderRule(isSplitScreenHeaderEnabled = false)

    @Test
    fun hasFullscreenHeader_fullscreenTaskOnDesktopFirstDisplay_isTrue() {
        assertTrue(
            withoutSplitHeader.hasFullscreenHeader(
                isDesktopFirstDisplay = true,
                isFullscreen = true,
                isInSplitScreen = false,
            )
        )
    }

    @Test
    fun hasFullscreenHeader_fullscreenTaskOnTouchFirstDisplay_isFalse() {
        assertFalse(
            withSplitHeader.hasFullscreenHeader(
                isDesktopFirstDisplay = false,
                isFullscreen = true,
                isInSplitScreen = false,
            )
        )
    }

    @Test
    fun hasFullscreenHeader_splitScreenTask_withSplitHeader_isTrue() {
        assertTrue(
            withSplitHeader.hasFullscreenHeader(
                isDesktopFirstDisplay = true,
                isFullscreen = false,
                isInSplitScreen = true,
            )
        )
    }

    @Test
    fun hasFullscreenHeader_splitScreenTask_withoutSplitHeader_isFalse() {
        assertFalse(
            withoutSplitHeader.hasFullscreenHeader(
                isDesktopFirstDisplay = true,
                isFullscreen = false,
                isInSplitScreen = true,
            )
        )
    }

    @Test
    fun hasFullscreenHeader_splitScreenTaskOnTouchFirstDisplay_isFalse() {
        assertFalse(
            withSplitHeader.hasFullscreenHeader(
                isDesktopFirstDisplay = false,
                isFullscreen = false,
                isInSplitScreen = true,
            )
        )
    }

    @Test
    fun hasFullscreenHeader_floatingTask_isFalse() {
        assertFalse(
            withSplitHeader.hasFullscreenHeader(
                isDesktopFirstDisplay = true,
                isFullscreen = false,
                isInSplitScreen = false,
            )
        )
    }
}
