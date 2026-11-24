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
package com.android.wm.shell.desktopmode

/**
 * Finds the desk that the keyboard shortcuts moving the focused window to the previous or the
 * next desk send it to.
 */
class FocusedWindowDeskResolver(
    private val previousDeskOf: (Int) -> Int?,
    private val nextDeskOf: (Int) -> Int?,
) {
    /** Which neighbor of the window's desk receives it. */
    enum class Direction {
        PREVIOUS,
        NEXT,
    }

    /**
     * Returns the neighbor in [direction] of [currentDeskId], the desk holding the window, or
     * null when the window is on no desk or there is no desk on that side.
     */
    fun findTargetDeskId(currentDeskId: Int?, direction: Direction): Int? {
        val deskId = currentDeskId ?: return null
        return when (direction) {
            Direction.PREVIOUS -> previousDeskOf(deskId)
            Direction.NEXT -> nextDeskOf(deskId)
        }
    }
}
