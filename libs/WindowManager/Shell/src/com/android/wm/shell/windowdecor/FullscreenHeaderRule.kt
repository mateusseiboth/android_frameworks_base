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

/** Which tasks of a desktop-first display get the fullscreen header (app chip, exit, close). */
class FullscreenHeaderRule(private val isSplitScreenHeaderEnabled: Boolean) {

    /**
     * Whether a task gets the fullscreen header: a fullscreen task on a desktop-first display, and
     * with [isSplitScreenHeaderEnabled] also a split-screen task there.
     */
    fun hasFullscreenHeader(
        isDesktopFirstDisplay: Boolean,
        isFullscreen: Boolean,
        isInSplitScreen: Boolean,
    ): Boolean = isDesktopFirstDisplay && (isFullscreen || isSplitScreenWithHeader(isInSplitScreen))

    private fun isSplitScreenWithHeader(isInSplitScreen: Boolean): Boolean =
        isSplitScreenHeaderEnabled && isInSplitScreen
}
