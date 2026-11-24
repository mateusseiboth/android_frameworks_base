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
import android.content.Context
import android.os.RemoteException
import android.os.SystemClock
import android.window.TaskSnapshotManager
import com.android.internal.protolog.ProtoLog
import com.android.wm.shell.R
import com.android.wm.shell.common.ShellExecutor
import com.android.wm.shell.protolog.ShellProtoLogGroup.WM_SHELL_DESKTOP_MODE
import com.android.wm.shell.shared.FocusTransitionListener
import com.android.wm.shell.shared.annotations.ShellBackgroundThread
import com.android.wm.shell.shared.annotations.ShellMainThread
import com.android.wm.shell.sysui.ShellInit
import com.android.wm.shell.transition.FocusTransitionObserver

/**
 * Captures a new snapshot of a desktop window when it loses focus, so the thumbnails of Alt+Tab
 * and Overview show what the window held when the user left it.
 */
class DesktopTaskFocusSnapshotHandler
@JvmOverloads
constructor(
    private val context: Context,
    private val focusTransitionObserver: FocusTransitionObserver,
    private val userRepositories: DesktopUserRepositories,
    private val taskSnapshotManager: TaskSnapshotManager,
    @ShellMainThread private val mainExecutor: ShellExecutor,
    @ShellBackgroundThread private val bgExecutor: ShellExecutor,
    shellInit: ShellInit,
    private val clock: () -> Long = SystemClock::elapsedRealtime,
) : FocusTransitionListener {
    private val policy = DesktopTaskFocusSnapshotPolicy(MIN_SNAPSHOT_INTERVAL_MS)

    init {
        shellInit.addInitCallback(::onInit, this)
    }

    private fun onInit() {
        if (!context.resources.getBoolean(R.bool.config_desktopSnapshotOnFocusLoss)) return
        focusTransitionObserver.setLocalFocusTransitionListener(this, mainExecutor)
    }

    override fun onFocusedTaskChanged(
        taskInfo: RunningTaskInfo,
        isFocusedOnDisplay: Boolean,
        isFocusedGlobally: Boolean,
    ) {
        val taskId = taskInfo.taskId
        val repository = userRepositories.getProfile(taskInfo.userId)
        val shouldSnapshot =
            policy.shouldSnapshot(
                taskId,
                isFocusedGlobally,
                isVisibleDesktopTask = repository.isVisibleTask(taskId),
                nowMillis = clock(),
            )
        if (!shouldSnapshot) return
        // The capture is a synchronous call into the system server: keep it off the main thread.
        bgExecutor.execute { takeSnapshot(taskId) }
    }

    private fun takeSnapshot(taskId: Int) {
        try {
            ProtoLog.d(WM_SHELL_DESKTOP_MODE, "%s: snapshot of taskId=%d (lost focus)", TAG, taskId)
            taskSnapshotManager.takeTaskSnapshot(taskId, /* updateCache= */ true)
        } catch (e: RemoteException) {
            ProtoLog.e(WM_SHELL_DESKTOP_MODE, "%s: snapshot of taskId=%d failed: %s", TAG, taskId, e)
        }
    }

    private companion object {
        const val TAG = "DesktopTaskFocusSnapshotHandler"
        const val MIN_SNAPSHOT_INTERVAL_MS = 1_000L
    }
}
