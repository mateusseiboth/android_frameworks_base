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

class StatusBarHoverHeaderRevealTest {

    private fun isShown(hoverX: Float?, left: Int = 0, right: Int = 960, top: Int = 0) =
        StatusBarHoverHeaderReveal.isHeaderShown(
            hoverX = hoverX,
            taskLeft = left,
            taskRight = right,
            taskTop = top,
            statusBarBottom = 36,
        )

    @Test
    fun noHover_keepsHeader() {
        assertTrue(isShown(hoverX = null))
    }

    @Test
    fun hoverAboveTask_showsHeader() {
        assertTrue(isShown(hoverX = 400f))
    }

    @Test
    fun hoverOnTaskEdges_showsHeader() {
        assertTrue(isShown(hoverX = 0f))
        assertTrue(isShown(hoverX = 960f))
    }

    @Test
    fun hoverAboveAnotherTask_hidesHeader() {
        assertFalse(isShown(hoverX = 1400f))
    }

    @Test
    fun taskBelowTheStatusBar_hidesHeader() {
        assertFalse(isShown(hoverX = 400f, top = 540))
    }

    @Test
    fun taskRightBelowTheStatusBar_showsHeader() {
        assertTrue(isShown(hoverX = 400f, top = 36))
    }
}
