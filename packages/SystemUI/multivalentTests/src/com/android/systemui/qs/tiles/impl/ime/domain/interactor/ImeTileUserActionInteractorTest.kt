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

package com.android.systemui.qs.tiles.impl.ime.domain.interactor

import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.systemui.SysuiTestCase
import com.android.systemui.inputmethod.data.repository.InputMethodRepository
import com.android.systemui.inputmethod.domain.interactor.InputMethodInteractor
import com.android.systemui.kosmos.testScope
import com.android.systemui.plugins.ActivityStarter
import com.android.systemui.qs.pipeline.domain.interactor.PanelInteractor
import com.android.systemui.qs.tiles.base.domain.actions.FakeQSTileIntentUserInputHandler
import com.android.systemui.qs.tiles.base.domain.actions.QSTileIntentUserInputHandlerSubject
import com.android.systemui.qs.tiles.base.domain.model.QSTileInputTestKtx
import com.android.systemui.qs.tiles.impl.ime.domain.model.ImeTileModel
import com.android.systemui.testKosmos
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking

@SmallTest
@RunWith(AndroidJUnit4::class)
class ImeTileUserActionInteractorTest : SysuiTestCase() {
    private val kosmos = testKosmos()
    private val testScope = kosmos.testScope
    private val inputHandler = FakeQSTileIntentUserInputHandler()
    private val inputMethodRepository = mock<InputMethodRepository>()
    private val panelInteractor = mock<PanelInteractor>()
    private val activityStarter = mock<ActivityStarter>()

    private lateinit var underTest: ImeTileUserActionInteractor

    @Before
    fun setUp() {
        underTest =
            ImeTileUserActionInteractor(
                context,
                testScope.backgroundScope,
                InputMethodInteractor(inputMethodRepository),
                inputHandler,
                panelInteractor,
                activityStarter,
            )
    }

    @Test
    fun handleClick_collapsesTheShadeAndShowsThePicker() =
        testScope.runTest {
            underTest.handleInput(QSTileInputTestKtx.click(ImeTileModel))

            val captor = argumentCaptor<Runnable>()
            verify(activityStarter).postQSRunnableDismissingKeyguard(captor.capture())
            captor.lastValue.run()
            runCurrent()
            verify(panelInteractor).collapsePanels()
            verifyBlocking(inputMethodRepository) {
                showInputMethodPicker(
                    showAuxiliarySubtypes = true,
                    entryPoint = InputMethodManager.IM_PICKER_ENTRY_POINT_DEFAULT,
                    displayId = context.displayId,
                )
            }
        }

    @Test
    fun handleLongClick_opensInputMethodSettings() =
        testScope.runTest {
            underTest.handleInput(QSTileInputTestKtx.longClick(ImeTileModel))

            QSTileIntentUserInputHandlerSubject.assertThat(inputHandler).handledOneIntentInput {
                assertThat(it.intent.action).isEqualTo(Settings.ACTION_INPUT_METHOD_SETTINGS)
            }
        }
}
