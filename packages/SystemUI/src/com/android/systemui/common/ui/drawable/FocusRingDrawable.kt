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

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

/**
 * Draws a Material 3 focus ring while its state holds [android.R.attr.state_focused], and nothing
 * otherwise. Meant as a view's foreground: the ring stays inside the view's bounds.
 */
class FocusRingDrawable(
    @ColorInt color: Int,
    private val strokeWidth: Float,
    private val gap: Float,
    shape: FocusRingShape,
) : Drawable() {

    var shape: FocusRingShape = shape
        set(value) {
            field = value
            invalidateSelf()
        }

    private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = this@FocusRingDrawable.strokeWidth
            this.color = color
        }

    private var isFocused = false

    override fun draw(canvas: Canvas) {
        if (!isFocused) return
        val ring =
            FocusRingGeometry.find(
                width = bounds.width().toFloat(),
                height = bounds.height().toFloat(),
                shape = shape,
                strokeWidth = strokeWidth,
                gap = gap,
            )
        canvas.drawRoundRect(
            bounds.left + ring.left,
            bounds.top + ring.top,
            bounds.left + ring.right,
            bounds.top + ring.bottom,
            ring.radius,
            ring.radius,
            paint,
        )
    }

    override fun isStateful(): Boolean = true

    // The ring is the focus indicator, so the view does not add its default focus highlight.
    override fun hasFocusStateSpecified(): Boolean = true

    override fun onStateChange(state: IntArray): Boolean {
        val focused = state.contains(android.R.attr.state_focused)
        if (focused == isFocused) return false
        isFocused = focused
        return true
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
