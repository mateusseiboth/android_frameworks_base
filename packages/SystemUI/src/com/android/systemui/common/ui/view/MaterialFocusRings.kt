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

package com.android.systemui.common.ui.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Insets
import android.graphics.drawable.LayerDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.children
import com.android.internal.widget.EmphasizedNotificationButton
import com.android.systemui.common.ui.drawable.FocusRingDrawable
import com.android.systemui.common.ui.drawable.FocusRingShape
import com.android.systemui.res.R

/**
 * Material 3 keyboard focus rings for view based SystemUI surfaces that had none: notification
 * action, expand and smart reply buttons, and dialog buttons. They share one ring color, stroke
 * width and gap; each surface gives the shape the ring follows. Nothing changes unless
 * config_materialFocusRings is set.
 */
object MaterialFocusRings {

    private val DIALOG_BUTTONS =
        listOf(
            DialogInterface.BUTTON_POSITIVE,
            DialogInterface.BUTTON_NEGATIVE,
            DialogInterface.BUTTON_NEUTRAL,
        )

    /** Draws a ring around [view] while it has keyboard focus. Calling it again updates [shape]. */
    @JvmStatic
    fun applyTo(view: View, shape: FocusRingShape) {
        val resources = view.resources
        if (!resources.getBoolean(R.bool.config_materialFocusRings)) return
        val existing = view.getTag(R.id.material_focus_ring) as? FocusRingDrawable
        if (existing != null) {
            existing.shape = shape
            return
        }
        val ring =
            FocusRingDrawable(
                color = view.context.getColor(com.android.internal.R.color.materialColorSecondary),
                strokeWidth = resources.getDimension(R.dimen.material_focus_ring_stroke_width),
                gap = resources.getDimension(R.dimen.material_focus_ring_gap),
                shape = shape,
            )
        view.foreground = view.foreground?.let { LayerDrawable(arrayOf(it, ring)) } ?: ring
        view.defaultFocusHighlightEnabled = false
        view.setTag(R.id.material_focus_ring, ring)
    }

    /**
     * Rings for a notification's action buttons: emphasized ones are pills, the others follow the
     * theme's button corners.
     */
    @JvmStatic
    fun applyToNotificationActions(actions: ViewGroup?) {
        actions ?: return
        val standardRadius = findButtonCornerRadius(actions)
        actions.children.filterIsInstance<Button>().forEach { button ->
            val radius =
                when (button) {
                    is EmphasizedNotificationButton -> FocusRingShape.FULLY_ROUNDED
                    else -> standardRadius
                }
            applyTo(button, FocusRingShape(cornerRadius = radius))
        }
    }

    /** A round ring for a notification's expand button. */
    @JvmStatic
    fun applyToNotificationExpandButton(expandButton: View?) {
        expandButton ?: return
        applyTo(expandButton, FocusRingShape(cornerRadius = FocusRingShape.FULLY_ROUNDED))
    }

    /** A ring for a smart reply or smart action chip, which is inset vertically in its button. */
    @JvmStatic
    fun applyToSmartReplyButton(button: Button) {
        val resources = button.resources
        val radius =
            resources.getDimension(R.dimen.notification_2025_smart_reply_button_corner_radius)
        val inset = resources.getDimension(R.dimen.notification_2025_smart_reply_button_inset)
        applyTo(button, FocusRingShape(cornerRadius = radius, insetTop = inset, insetBottom = inset))
    }

    /** Pill rings for a dialog's buttons, following their background's insets. */
    @JvmStatic
    fun applyToDialogButtons(dialog: AlertDialog) {
        DIALOG_BUTTONS.mapNotNull { dialog.getButton(it) }
            .forEach { button ->
                val insets = button.background?.opticalInsets ?: Insets.NONE
                applyTo(
                    button,
                    FocusRingShape(
                        cornerRadius = FocusRingShape.FULLY_ROUNDED,
                        insetLeft = insets.left.toFloat(),
                        insetTop = insets.top.toFloat(),
                        insetRight = insets.right.toFloat(),
                        insetBottom = insets.bottom.toFloat(),
                    ),
                )
            }
    }

    private fun findButtonCornerRadius(view: View): Float {
        val fallback = view.resources.getDimension(R.dimen.notification_fallback_focus_ring_radius)
        return view.context.obtainStyledAttributes(intArrayOf(android.R.attr.buttonCornerRadius)).use {
            it.getDimension(0, fallback)
        }
    }
}
