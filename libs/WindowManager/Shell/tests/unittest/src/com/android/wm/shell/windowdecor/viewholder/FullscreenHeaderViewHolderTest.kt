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

package com.android.wm.shell.windowdecor.viewholder

import android.testing.AndroidTestingRunner
import android.view.View
import androidx.test.filters.SmallTest
import com.android.wm.shell.R
import com.android.wm.shell.ShellTestCase
import com.google.common.truth.Truth.assertThat
import kotlin.test.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

/**
 * Tests for [FullscreenHeaderViewHolder].
 *
 * Build/Install/Run: atest WMShellUnitTests:FullscreenHeaderViewHolderTest
 */
@SmallTest
@RunWith(AndroidTestingRunner::class)
class FullscreenHeaderViewHolderTest : ShellTestCase() {

    @Test
    fun header_hasNoMinimizeButton() {
        val holder =
            FullscreenHeaderViewHolder.DefaultFactory()
                .create(
                    context = mContext,
                    windowDecorationActions = mock(),
                    gestureInterceptor = mock(),
                    onLongClickListener = mock(),
                    onCaptionGenericMotionListener = mock(),
                    onExitFullscreenHoverAnimationFinishedListener = {},
                    desktopModeUiEventLogger = mock(),
                    dimensions = mock(),
                    focusTransitionObserver = mock(),
                )

        // A fullscreen app leaves through the exit fullscreen or close buttons.
        assertThat(holder.rootView.requireViewById<View>(R.id.minimize_window).visibility)
            .isEqualTo(View.GONE)
        assertThat(holder.rootView.requireViewById<View>(R.id.close_window).visibility)
            .isEqualTo(View.VISIBLE)
    }
}
