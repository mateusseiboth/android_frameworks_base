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

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.widget.Button
import android.widget.LinearLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.internal.widget.EmphasizedNotificationButton
import com.android.systemui.SysuiTestCase
import com.android.systemui.common.ui.drawable.FocusRingDrawable
import com.android.systemui.common.ui.drawable.FocusRingShape
import com.android.systemui.res.R
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class MaterialFocusRingsTest : SysuiTestCase() {

    @Before
    fun setUp() {
        MaterialFocusRings.install(context)
    }

    // A notification's views are inflated with the posting app's context, whose resources do not
    // have SystemUI's: the config and dimensions must come from SystemUI's own.
    @Test
    fun applyTo_viewOfAnotherPackage_usesSystemUiResources() {
        setMaterialFocusRings(true)
        val appContext = context.createPackageContext("android", 0)
        val button = Button(appContext)

        MaterialFocusRings.applyTo(button, FocusRingShape(cornerRadius = 4f))

        assertThat(button.foreground).isInstanceOf(FocusRingDrawable::class.java)
    }

    @Test
    fun applyToNotificationActions_viewsOfAnotherPackage_getRings() {
        setMaterialFocusRings(true)
        val appContext = context.createPackageContext("android", 0)
        val button = Button(appContext)
        val actions = LinearLayout(appContext).apply { addView(button) }

        MaterialFocusRings.applyToNotificationActions(actions)

        assertThat(button.foreground).isInstanceOf(FocusRingDrawable::class.java)
    }

    @Test
    fun applyTo_configOff_leavesTheViewAlone() {
        setMaterialFocusRings(false)
        val button = Button(context)

        MaterialFocusRings.applyTo(button, FocusRingShape(cornerRadius = 4f))

        assertThat(button.foreground).isNull()
        assertThat(button.defaultFocusHighlightEnabled).isTrue()
    }

    @Test
    fun applyTo_configOn_ringReplacesTheDefaultHighlight() {
        setMaterialFocusRings(true)
        val button = Button(context)

        MaterialFocusRings.applyTo(button, FocusRingShape(cornerRadius = 4f))

        assertThat(button.foreground).isInstanceOf(FocusRingDrawable::class.java)
        assertThat(button.defaultFocusHighlightEnabled).isFalse()
    }

    @Test
    fun applyTo_twice_keepsOneRingWithTheNewShape() {
        setMaterialFocusRings(true)
        val button = Button(context)

        MaterialFocusRings.applyTo(button, FocusRingShape(cornerRadius = 4f))
        val ring = button.foreground as FocusRingDrawable
        MaterialFocusRings.applyTo(button, FocusRingShape(cornerRadius = 8f))

        assertThat(button.foreground).isSameInstanceAs(ring)
        assertThat(ring.shape.cornerRadius).isEqualTo(8f)
    }

    @Test
    fun applyTo_existingForeground_keptUnderTheRing() {
        setMaterialFocusRings(true)
        val button = Button(context).apply { foreground = ColorDrawable() }

        MaterialFocusRings.applyTo(button, FocusRingShape(cornerRadius = 4f))

        assertThat(button.foreground).isInstanceOf(LayerDrawable::class.java)
    }

    @Test
    fun applyToNotificationActions_emphasizedButtonsArePills() {
        setMaterialFocusRings(true)
        val emphasized = EmphasizedNotificationButton(context)
        val standard = Button(context)
        val actions = LinearLayout(context).apply {
            addView(emphasized)
            addView(standard)
        }

        MaterialFocusRings.applyToNotificationActions(actions)

        assertThat((emphasized.foreground as FocusRingDrawable).shape.cornerRadius)
            .isEqualTo(FocusRingShape.FULLY_ROUNDED)
        assertThat((standard.foreground as FocusRingDrawable).shape.cornerRadius)
            .isLessThan(FocusRingShape.FULLY_ROUNDED)
    }

    @Test
    fun focusRingDrawable_followsTheFocusedState() {
        val ring =
            FocusRingDrawable(color = 0, strokeWidth = 3f, gap = 2f, FocusRingShape(4f))

        assertThat(ring.setState(intArrayOf(android.R.attr.state_focused))).isTrue()
        assertThat(ring.setState(intArrayOf(android.R.attr.state_focused))).isFalse()
        assertThat(ring.setState(intArrayOf())).isTrue()
        assertThat(ring.hasFocusStateSpecified()).isTrue()
    }

    private fun setMaterialFocusRings(enabled: Boolean) {
        context.orCreateTestableResources.addOverride(R.bool.config_materialFocusRings, enabled)
    }
}
