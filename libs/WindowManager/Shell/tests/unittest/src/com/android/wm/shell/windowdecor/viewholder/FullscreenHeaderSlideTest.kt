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
package com.android.wm.shell.windowdecor.viewholder

import org.junit.Assert.assertEquals
import org.junit.Test

/** Tests for [FullscreenHeaderSlide]. */
class FullscreenHeaderSlideTest {

    private val slide = FullscreenHeaderSlide()

    @Test
    fun duration_is250Ms() {
        assertEquals(250L, FullscreenHeaderSlide.DURATION_MS)
    }

    @Test
    fun hiddenOffset_isTheHeaderHeightAboveTheTop() {
        assertEquals(-120f, slide.hiddenOffset(height = 120), 0f)
    }

    @Test
    fun plan_showFromHidden_slidesDownTheWholeHeightInTheFullDuration() {
        val plan = slide.plan(isShowing = true, currentY = -120f, height = 120)

        assertEquals(FullscreenHeaderSlide.Plan(fromY = -120f, toY = 0f, durationMs = 250), plan)
    }

    @Test
    fun plan_hideFromShown_slidesUpTheWholeHeightInTheFullDuration() {
        val plan = slide.plan(isShowing = false, currentY = 0f, height = 120)

        assertEquals(FullscreenHeaderSlide.Plan(fromY = 0f, toY = -120f, durationMs = 250), plan)
    }

    @Test
    fun plan_reversedHalfway_takesHalfTheDurationFromWhereTheHeaderIs() {
        val plan = slide.plan(isShowing = false, currentY = -60f, height = 120)

        assertEquals(FullscreenHeaderSlide.Plan(fromY = -60f, toY = -120f, durationMs = 125), plan)
    }

    @Test
    fun plan_alreadyAtTarget_takesNoTime() {
        val plan = slide.plan(isShowing = true, currentY = 0f, height = 120)

        assertEquals(FullscreenHeaderSlide.Plan(fromY = 0f, toY = 0f, durationMs = 0), plan)
    }

    @Test
    fun plan_offsetBeyondTheHeight_isClampedToTheFullDuration() {
        val plan = slide.plan(isShowing = true, currentY = -300f, height = 120)

        assertEquals(FullscreenHeaderSlide.Plan(fromY = -120f, toY = 0f, durationMs = 250), plan)
    }

    @Test
    fun plan_withoutHeight_jumpsToTheTarget() {
        val plan = slide.plan(isShowing = false, currentY = 0f, height = 0)

        assertEquals(FullscreenHeaderSlide.Plan(fromY = 0f, toY = 0f, durationMs = 0), plan)
    }
}
