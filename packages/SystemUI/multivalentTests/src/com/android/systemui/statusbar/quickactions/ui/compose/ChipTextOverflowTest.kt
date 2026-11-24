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

package com.android.systemui.statusbar.quickactions.ui.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChipTextOverflowTest {

    @Test
    fun textThatFits_neitherFadesNorScrolls() {
        val style = chipTextOverflowStyle(isTitleChip = true, isHovered = true, hasOverflow = false)

        assertEquals(ChipTextFadeEdges.NONE, style.fadeEdges)
        assertFalse(style.isScrolling)
    }

    @Test
    fun defaultChip_fadesTheEndEvenWhenHovered() {
        val style = chipTextOverflowStyle(isTitleChip = false, isHovered = true, hasOverflow = true)

        assertEquals(ChipTextFadeEdges.END, style.fadeEdges)
        assertFalse(style.isScrolling)
    }

    @Test
    fun titleChip_fadesTheEndAtRest() {
        val style = chipTextOverflowStyle(isTitleChip = true, isHovered = false, hasOverflow = true)

        assertEquals(ChipTextFadeEdges.END, style.fadeEdges)
        assertFalse(style.isScrolling)
    }

    @Test
    fun titleChip_scrollsAndFadesBothEdgesWhileHovered() {
        val style = chipTextOverflowStyle(isTitleChip = true, isHovered = true, hasOverflow = true)

        assertEquals(ChipTextFadeEdges.BOTH, style.fadeEdges)
        assertTrue(style.isScrolling)
    }

    @Test
    fun hoverIcons_replaceTheIconsOnlyWhileHovered() {
        val icons = listOf("app")
        val hoverIcons = listOf("pause")

        assertEquals(hoverIcons, chipIconsFor(icons, hoverIcons, isHovered = true))
        assertEquals(icons, chipIconsFor(icons, hoverIcons, isHovered = false))
    }

    @Test
    fun withoutHoverIcons_theIconsStayWhileHovered() {
        val icons = listOf("app")

        assertEquals(icons, chipIconsFor(icons, hoverIcons = null, isHovered = true))
    }
}
