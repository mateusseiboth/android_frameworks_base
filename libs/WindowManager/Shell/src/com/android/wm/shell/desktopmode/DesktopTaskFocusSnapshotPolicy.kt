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
 * Decides which desktop window gets a new snapshot when it loses focus, at most once every
 * [minIntervalMillis] per window, so a quick back and forth between windows does not capture
 * them over and over.
 */
class DesktopTaskFocusSnapshotPolicy(private val minIntervalMillis: Long) {
    private val lastSnapshotTimes = mutableMapOf<Int, Long>()

    /**
     * Returns whether the window of [taskId] should be captured now that its focus changed, and
     * when it should, starts its interval at [nowMillis].
     */
    fun shouldSnapshot(
        taskId: Int,
        isFocusedGlobally: Boolean,
        isVisibleDesktopTask: Boolean,
        nowMillis: Long,
    ): Boolean {
        if (isFocusedGlobally) return false
        if (!isVisibleDesktopTask) return false
        forgetExpired(nowMillis)
        if (lastSnapshotTimes.containsKey(taskId)) return false
        lastSnapshotTimes[taskId] = nowMillis
        return true
    }

    private fun forgetExpired(nowMillis: Long) {
        lastSnapshotTimes.values.removeAll { nowMillis - it >= minIntervalMillis }
    }
}
