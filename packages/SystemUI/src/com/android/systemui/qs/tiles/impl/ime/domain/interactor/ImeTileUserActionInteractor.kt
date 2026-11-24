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

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import com.android.systemui.animation.Expandable
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.inputmethod.domain.interactor.InputMethodInteractor
import com.android.systemui.plugins.ActivityStarter
import com.android.systemui.qs.pipeline.domain.interactor.PanelInteractor
import com.android.systemui.qs.tiles.base.domain.actions.QSTileIntentUserInputHandler
import com.android.systemui.qs.tiles.base.domain.interactor.QSTileUserActionInteractor
import com.android.systemui.qs.tiles.base.domain.model.QSTileInput
import com.android.systemui.qs.tiles.base.shared.model.QSTileUserAction
import com.android.systemui.qs.tiles.impl.ime.domain.model.ImeTileModel
import com.android.systemui.shade.ShadeDisplayAware
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Handles input method tile clicks. */
class ImeTileUserActionInteractor
@Inject
constructor(
    @ShadeDisplayAware private val context: Context,
    @Background private val backgroundScope: CoroutineScope,
    private val inputMethodInteractor: InputMethodInteractor,
    private val qsTileIntentUserInputHandler: QSTileIntentUserInputHandler,
    private val panelInteractor: PanelInteractor,
    private val activityStarter: ActivityStarter,
) : QSTileUserActionInteractor<ImeTileModel> {

    val longClickIntent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)

    override suspend fun handleInput(input: QSTileInput<ImeTileModel>) {
        when (input.action) {
            is QSTileUserAction.Click -> handleClick()
            is QSTileUserAction.LongClick -> handleLongClick(input.action.expandable)
            is QSTileUserAction.ToggleClick -> {}
        }
    }

    /** Closes the shade, which would cover the picker, and opens the input method picker. */
    fun handleClick() {
        val displayId = context.displayId
        activityStarter.postQSRunnableDismissingKeyguard {
            panelInteractor.collapsePanels()
            backgroundScope.launch {
                inputMethodInteractor.showInputMethodPicker(
                    showAuxiliarySubtypes = true,
                    entryPoint = InputMethodManager.IM_PICKER_ENTRY_POINT_DEFAULT,
                    displayId = displayId,
                )
            }
        }
    }

    fun handleLongClick(expandable: Expandable?) {
        qsTileIntentUserInputHandler.handle(expandable, longClickIntent)
    }
}
