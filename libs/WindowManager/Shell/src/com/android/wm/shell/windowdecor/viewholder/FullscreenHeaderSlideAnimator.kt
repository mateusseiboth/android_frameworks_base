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

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.view.doOnNextLayout
import com.android.wm.shell.shared.animation.Interpolators

/**
 * Shows and hides the fullscreen header. When [isAnimated], the header slides down from above the
 * top edge and back up (translationY, FAST_OUT_SLOW_IN, timing from [FullscreenHeaderSlide]) and
 * only becomes GONE once it is out of sight; otherwise it appears and disappears at once.
 */
class FullscreenHeaderSlideAnimator(
    private val view: View,
    private val isAnimated: Boolean,
    private val slide: FullscreenHeaderSlide = FullscreenHeaderSlide(),
) {
    private var isTargetVisible = view.visibility == View.VISIBLE
    private var animator: Animator? = null
    private var isWaitingForLayout = false

    /** Shows or hides the header. */
    fun setVisible(isVisible: Boolean) {
        if (!isAnimated) return setVisibilityAtOnce(isVisible)
        if (isVisible == isTargetVisible) return
        isTargetVisible = isVisible
        if (!ValueAnimator.areAnimatorsEnabled()) return jumpTo(isVisible)
        if (view.height == 0) return slideAfterLayout(isVisible)
        slideTo(isVisible)
    }

    /** Stops any slide in progress, leaving the header where it is. */
    fun cancel() {
        val running = animator
        animator = null
        running?.cancel()
    }

    private fun setVisibilityAtOnce(isVisible: Boolean) {
        isTargetVisible = isVisible
        view.visibility = visibilityOf(isVisible)
    }

    private fun jumpTo(isVisible: Boolean) {
        cancel()
        view.translationY = slide.plan(isVisible, view.translationY, view.height).toY
        view.visibility = visibilityOf(isVisible)
    }

    /** A header never laid out has no height to slide by: lay it out unseen, then slide it. */
    private fun slideAfterLayout(isVisible: Boolean) {
        if (!isVisible) return jumpTo(isVisible = false)
        view.visibility = View.INVISIBLE
        if (isWaitingForLayout) return
        isWaitingForLayout = true
        view.doOnNextLayout {
            isWaitingForLayout = false
            if (isTargetVisible) slideTo(isVisible = true)
        }
    }

    private fun slideTo(isVisible: Boolean) {
        cancel()
        val plan = slide.plan(isVisible, currentOffset(), view.height)
        view.translationY = plan.fromY
        view.visibility = View.VISIBLE
        val slideAnimator =
            ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, plan.fromY, plan.toY).apply {
                duration = plan.durationMs
                interpolator = Interpolators.FAST_OUT_SLOW_IN
            }
        slideAnimator.doOnEnd { onSlideEnd(it) }
        animator = slideAnimator
        slideAnimator.start()
    }

    /** Where the header is now: a header that is not shown starts from above the top edge. */
    private fun currentOffset(): Float =
        when (view.visibility) {
            View.VISIBLE -> view.translationY
            else -> slide.hiddenOffset(view.height)
        }

    private fun onSlideEnd(ended: Animator) {
        if (ended !== animator) return
        animator = null
        view.visibility = visibilityOf(isTargetVisible)
    }

    private fun visibilityOf(isVisible: Boolean): Int =
        when {
            isVisible -> View.VISIBLE
            else -> View.GONE
        }
}
