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

import android.testing.AndroidTestingRunner
import android.window.TaskSnapshotManager
import androidx.test.filters.SmallTest
import com.android.wm.shell.R
import com.android.wm.shell.ShellTestCase
import com.android.wm.shell.TestShellExecutor
import com.android.wm.shell.desktopmode.DesktopTestHelpers.createFreeformTask
import com.android.wm.shell.desktopmode.data.DesktopRepository
import com.android.wm.shell.sysui.ShellInit
import com.android.wm.shell.transition.FocusTransitionObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for [DesktopTaskFocusSnapshotHandler].
 *
 * Usage: atest WMShellUnitTests:DesktopTaskFocusSnapshotHandlerTest
 */
@SmallTest
@RunWith(AndroidTestingRunner::class)
class DesktopTaskFocusSnapshotHandlerTest : ShellTestCase() {
    private val focusTransitionObserver = mock<FocusTransitionObserver>()
    private val userRepositories = mock<DesktopUserRepositories>()
    private val repository = mock<DesktopRepository>()
    private val taskSnapshotManager = mock<TaskSnapshotManager>()
    private val mainExecutor = TestShellExecutor()
    private val bgExecutor = TestShellExecutor()
    private val shellInit = ShellInit(mainExecutor)
    private var now = 0L

    private lateinit var handler: DesktopTaskFocusSnapshotHandler

    @Before
    fun setUp() {
        whenever(userRepositories.getProfile(any())).thenReturn(repository)
    }

    @Test
    fun init_enabled_listensToFocusChanges() {
        createHandler(isEnabled = true)

        verify(focusTransitionObserver).setLocalFocusTransitionListener(handler, mainExecutor)
    }

    @Test
    fun init_disabled_doesNotListen() {
        createHandler(isEnabled = false)

        verify(focusTransitionObserver, never()).setLocalFocusTransitionListener(any(), any())
    }

    @Test
    fun visibleDesktopTaskLosesFocus_takesSnapshot() {
        createHandler(isEnabled = true)
        val task = createFreeformTask(taskId = TASK_ID)
        whenever(repository.isVisibleTask(TASK_ID)).thenReturn(true)

        handler.onFocusedTaskChanged(task, false, false)
        bgExecutor.flushAll()

        verify(taskSnapshotManager).takeTaskSnapshot(TASK_ID, true)
    }

    @Test
    fun desktopTaskGainsFocus_noSnapshot() {
        createHandler(isEnabled = true)
        val task = createFreeformTask(taskId = TASK_ID)
        whenever(repository.isVisibleTask(TASK_ID)).thenReturn(true)

        handler.onFocusedTaskChanged(task, true, true)
        bgExecutor.flushAll()

        verify(taskSnapshotManager, never()).takeTaskSnapshot(any(), any())
    }

    @Test
    fun hiddenTaskLosesFocus_noSnapshot() {
        createHandler(isEnabled = true)
        val task = createFreeformTask(taskId = TASK_ID)
        whenever(repository.isVisibleTask(TASK_ID)).thenReturn(false)

        handler.onFocusedTaskChanged(task, false, false)
        bgExecutor.flushAll()

        verify(taskSnapshotManager, never()).takeTaskSnapshot(any(), any())
    }

    @Test
    fun repeatedFocusLossWithinASecond_singleSnapshot() {
        createHandler(isEnabled = true)
        val task = createFreeformTask(taskId = TASK_ID)
        whenever(repository.isVisibleTask(TASK_ID)).thenReturn(true)

        handler.onFocusedTaskChanged(task, false, false)
        now += 500L
        handler.onFocusedTaskChanged(task, false, false)
        bgExecutor.flushAll()

        verify(taskSnapshotManager).takeTaskSnapshot(eq(TASK_ID), eq(true))
    }

    private fun createHandler(isEnabled: Boolean) {
        mContext.orCreateTestableResources.addOverride(
            R.bool.config_desktopSnapshotOnFocusLoss,
            isEnabled,
        )
        handler =
            DesktopTaskFocusSnapshotHandler(
                mContext,
                focusTransitionObserver,
                userRepositories,
                taskSnapshotManager,
                mainExecutor,
                bgExecutor,
                shellInit,
                clock = { now },
            )
        shellInit.init()
    }

    private companion object {
        const val TASK_ID = 5
    }
}
