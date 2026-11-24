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

/**
 * Which revealed fullscreen header shows while the pointer hovers the desktop status bar: only the
 * header of the task under the pointer, that is the task whose horizontal span holds the pointer's
 * x and whose top edge is under the status bar. Without a hover, every revealed header shows.
 */
object StatusBarHoverHeaderReveal {

    /**
     * @param hoverX the pointer's x on the status bar of the task's display, in display
     *   coordinates, or null when the pointer is not over the status bar.
     * @param statusBarBottom the bottom edge of the status bar, in display coordinates.
     */
    @JvmStatic
    fun isHeaderShown(
        hoverX: Float?,
        taskLeft: Int,
        taskRight: Int,
        taskTop: Int,
        statusBarBottom: Int,
    ): Boolean {
        val x = hoverX ?: return true
        if (taskTop > statusBarBottom) return false
        return x >= taskLeft && x <= taskRight
    }
}
