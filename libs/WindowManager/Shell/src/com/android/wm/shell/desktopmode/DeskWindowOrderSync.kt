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

import android.app.ActivityManager.RunningTaskInfo
import android.app.ActivityTaskManager
import com.android.wm.shell.desktopmode.data.DesktopRepository
import com.android.wm.shell.desktopmode.multidesks.DesksOrganizer

/**
 * Keeps the stacking order of a desk's windows across leaving and re-entering the desktop.
 *
 * The window manager keeps the children of an inactive desk in the order the user left them, but
 * the repository order (used to bring the windows back on re-entry) may have drifted, e.g. when a
 * window below others changed bounds. Before a desk is activated again, the repository takes the
 * order the window manager reports for the running windows of the desk.
 */
class DeskWindowOrderSync(
    private val isEnabled: Boolean,
    private val desksOrganizer: DesksOrganizer,
    private val runningTasksTopFirst: (displayId: Int) -> List<RunningTaskInfo> = { displayId ->
        ActivityTaskManager.getInstance()
            .getTasks(
                Int.MAX_VALUE,
                /* filterOnlyVisibleRecents= */ false,
                /* keepIntentExtra= */ false,
                displayId,
            )
    },
) {

    /** Applies the window manager's stacking of [deskId] to [repository]. */
    fun applyWindowOrder(repository: DesktopRepository, deskId: Int, displayId: Int) {
        if (!isEnabled) return
        val liveTopFirst =
            runningTasksTopFirst(displayId)
                .filter { task -> desksOrganizer.getDeskIdFromTaskInfo(task) == deskId }
                .map { task -> task.taskId }
        repository.applyWindowOrderToDesk(deskId, liveTopFirst)
    }
}
