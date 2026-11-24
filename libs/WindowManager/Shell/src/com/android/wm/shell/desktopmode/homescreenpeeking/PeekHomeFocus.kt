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

package com.android.wm.shell.desktopmode.homescreenpeeking

/**
 * Tracks the desktop tasks made unfocusable while the home screen is peeked, so Home holds the
 * input focus, and orders their restore when the peek ends so the top task takes focus back.
 */
class PeekHomeFocus {
    private val unfocusableTaskIds = LinkedHashSet<Int>()

    /** Records the tasks, top first, made unfocusable by a peek. */
    fun onPeek(taskIdsTopFirst: List<Int>) {
        unfocusableTaskIds += taskIdsTopFirst
    }

    /** Returns the tasks that must become focusable again, and forgets them. */
    fun takeTasksToRestore(): List<Int> =
        unfocusableTaskIds.toList().also { unfocusableTaskIds.clear() }

    /** Whether bringing [taskId] to front, e.g. from the taskbar, must end the peek. */
    fun shouldUnpeekOnTaskToFront(taskId: Int): Boolean = taskId in unfocusableTaskIds

    companion object {
        /**
         * Returns the order in which the peeked tasks are moved back on top when the peek ends:
         * bottom first, so the stacking is kept, with [frontTaskId] (a task the user brought to
         * front) last so it ends on top.
         */
        fun buildReorderOrder(tasksTopFirst: List<Int>, frontTaskId: Int?): List<Int> {
            val bottomFirst = tasksTopFirst.reversed()
            if (frontTaskId == null || frontTaskId !in bottomFirst) return bottomFirst
            return bottomFirst - frontTaskId + frontTaskId
        }
    }
}
