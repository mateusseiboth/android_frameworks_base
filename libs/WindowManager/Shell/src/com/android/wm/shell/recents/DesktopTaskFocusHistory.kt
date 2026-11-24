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
package com.android.wm.shell.recents

import java.util.function.Predicate
import java.util.function.ToLongFunction

/**
 * Remembers when each task last got the global focus, so the desktop windows of the recent tasks
 * (and of Alt+Tab) can be ordered by the last one the user worked in.
 */
class DesktopTaskFocusHistory {
    private val lastFocusedTimes = mutableMapOf<Int, Long>()

    /** Records that [taskId] got the global focus at [time]. */
    fun onTaskFocused(taskId: Int, time: Long) {
        lastFocusedTimes[taskId] = time
    }

    /** Forgets [taskId] once it is gone. */
    fun onTaskRemoved(taskId: Int) {
        lastFocusedTimes.remove(taskId)
    }

    /** Returns when [taskId] last got focus, or [fallbackTime] if it never did since boot. */
    fun getLastFocusedTime(taskId: Int, fallbackTime: Long): Long =
        lastFocusedTimes[taskId] ?: fallbackTime

    /**
     * Returns [tasks] with the desk tasks (those matching [isDeskTask]) sorted by [timeOf], most
     * recent first, in the slots the desk tasks took. Every other task keeps its slot, and desk
     * tasks with the same time keep their given order.
     */
    fun <T> orderDeskTasks(
        tasks: List<T>,
        isDeskTask: Predicate<T>,
        timeOf: ToLongFunction<T>,
    ): List<T> {
        val deskTasks =
            tasks
                .filter { isDeskTask.test(it) }
                .sortedByDescending { timeOf.applyAsLong(it) }
                .iterator()
        return tasks.map { task ->
            when {
                isDeskTask.test(task) -> deskTasks.next()
                else -> task
            }
        }
    }
}
