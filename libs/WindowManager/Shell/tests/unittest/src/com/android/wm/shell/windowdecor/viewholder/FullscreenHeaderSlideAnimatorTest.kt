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

import android.animation.AnimatorTestRule
import android.testing.AndroidTestingRunner
import android.testing.TestableLooper
import android.view.View
import androidx.test.filters.SmallTest
import com.android.wm.shell.ShellTestCase
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for [FullscreenHeaderSlideAnimator].
 *
 * Build/Install/Run: atest WMShellUnitTests:FullscreenHeaderSlideAnimatorTest
 */
@SmallTest
@TestableLooper.RunWithLooper
@RunWith(AndroidTestingRunner::class)
class FullscreenHeaderSlideAnimatorTest : ShellTestCase() {

    @JvmField @Rule val animatorTestRule = AnimatorTestRule(this)

    private lateinit var view: View

    @Before
    fun setUp() {
        view = View(mContext).apply { layout(0, 0, WIDTH, HEIGHT) }
    }

    @Test
    fun hide_animated_slidesUpThenGone() {
        val animator = FullscreenHeaderSlideAnimator(view, isAnimated = true)

        animator.setVisible(false)
        animatorTestRule.advanceTimeBy(FullscreenHeaderSlide.DURATION_MS / 2)

        assertThat(view.visibility).isEqualTo(View.VISIBLE)
        assertThat(view.translationY).isLessThan(0f)

        animatorTestRule.advanceTimeBy(FullscreenHeaderSlide.DURATION_MS)

        assertThat(view.visibility).isEqualTo(View.GONE)
        assertThat(view.translationY).isEqualTo(-HEIGHT.toFloat())
    }

    @Test
    fun show_animated_slidesDownFromAboveTheTop() {
        val animator = FullscreenHeaderSlideAnimator(view, isAnimated = true)
        animator.setVisible(false)
        animatorTestRule.advanceTimeBy(FullscreenHeaderSlide.DURATION_MS)

        animator.setVisible(true)

        assertThat(view.visibility).isEqualTo(View.VISIBLE)
        assertThat(view.translationY).isEqualTo(-HEIGHT.toFloat())

        animatorTestRule.advanceTimeBy(FullscreenHeaderSlide.DURATION_MS)

        assertThat(view.translationY).isEqualTo(0f)
    }

    @Test
    fun showWhileHiding_animated_staysVisible() {
        val animator = FullscreenHeaderSlideAnimator(view, isAnimated = true)
        animator.setVisible(false)
        animatorTestRule.advanceTimeBy(FullscreenHeaderSlide.DURATION_MS / 2)

        animator.setVisible(true)
        animatorTestRule.advanceTimeBy(FullscreenHeaderSlide.DURATION_MS)

        assertThat(view.visibility).isEqualTo(View.VISIBLE)
        assertThat(view.translationY).isEqualTo(0f)
    }

    @Test
    fun hide_notAnimated_isGoneAtOnce() {
        val animator = FullscreenHeaderSlideAnimator(view, isAnimated = false)

        animator.setVisible(false)

        assertThat(view.visibility).isEqualTo(View.GONE)
        assertThat(view.translationY).isEqualTo(0f)
    }

    private companion object {
        const val WIDTH = 800
        const val HEIGHT = 120
    }
}
