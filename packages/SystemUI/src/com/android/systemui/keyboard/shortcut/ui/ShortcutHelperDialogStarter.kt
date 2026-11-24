/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.systemui.keyboard.shortcut.ui

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.Configuration
import android.os.Bundle
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.compose.modifiers.thenIf
import com.android.systemui.dagger.qualifiers.Main
import com.android.systemui.display.dagger.SystemUIDisplaySubcomponent.DisplayAware
import com.android.systemui.display.dagger.SystemUIDisplaySubcomponent.LifecycleListener
import com.android.systemui.display.dagger.SystemUIDisplaySubcomponent.PerDisplaySingleton
import com.android.systemui.keyboard.shortcut.ui.composable.ShortcutHelper
import com.android.systemui.keyboard.shortcut.ui.composable.ShortcutHelperBottomSheet
import com.android.systemui.keyboard.shortcut.ui.composable.ShortcutHelperDualPaneLayout
import com.android.systemui.keyboard.shortcut.ui.composable.getWidth
import com.android.systemui.keyboard.shortcut.ui.composable.hasExpandedWindowHeight
import com.android.systemui.keyboard.shortcut.ui.viewmodel.ShortcutHelperViewModel
import com.android.systemui.lifecycle.rememberActivated
import com.android.systemui.plugins.ActivityStarter
import com.android.systemui.common.ui.compose.SelectedUserAwareInputConnection
import com.android.systemui.res.R
import com.android.systemui.settings.UserTracker
import com.android.systemui.statusbar.phone.DialogDelegate
import com.android.systemui.statusbar.phone.SystemUIDialog
import com.android.systemui.statusbar.phone.SystemUIDialogFactory
import com.android.systemui.statusbar.phone.create
import com.android.systemui.statusbar.phone.createBottomSheet
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@PerDisplaySingleton
class ShortcutHelperDialogStarter
@Inject
constructor(
    @DisplayAware private val displayScope: CoroutineScope,
    @DisplayAware private val displayContext: Context,
    @DisplayAware private val shortcutHelperViewModel: ShortcutHelperViewModel,
    private val shortcutCustomizationDialogStarterFactory:
        ShortcutCustomizationDialogStarter.Factory,
    private val dialogFactory: SystemUIDialogFactory,
    private val userTracker: UserTracker,
    private val activityStarter: ActivityStarter,
    @Main private val mainDispatcher: CoroutineDispatcher,
) : LifecycleListener {

    @VisibleForTesting var dialog: Dialog? = null

    override fun start() {
        shortcutHelperViewModel.shouldShow
            .map { shouldShow ->
                if (shouldShow) {
                    withContext(mainDispatcher) {
                        dialog = createShortcutHelperDialog().also { it.show() }
                    }
                } else {
                    dialog?.dismiss()
                }
            }
            .launchIn(displayScope)
    }

    private fun createShortcutHelperDialog(): Dialog {
        val dialog =
            when (val presentation = findPresentation()) {
                is ShortcutHelperPresentation.BottomSheet -> createBottomSheetDialog()
                is ShortcutHelperPresentation.Dialog -> createSidebarDialog(presentation.widthDp)
            }
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        return dialog
    }

    private fun findPresentation(): ShortcutHelperPresentation {
        val resources = displayContext.resources
        val configuration = resources.configuration
        return ShortcutHelperPresentation.find(
            isDialogEnabled = resources.getBoolean(R.bool.config_shortcutHelperAsDialog),
            screenWidthDp = configuration.screenWidthDp,
            screenHeightDp = configuration.screenHeightDp,
            isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
        )
    }

    private fun createBottomSheetDialog(): Dialog =
        dialogFactory.createBottomSheet(
            // The taskbar stays usable while the shortcuts are listed.
            shouldStashTaskbar = false,
            content = { dialog ->
                ShortcutHelperContent(
                    dialog = dialog,
                    modifier =
                        Modifier.width(getWidth()).thenIf(hasExpandedWindowHeight()) {
                            Modifier.fillMaxHeight(0.72f)
                        },
                    dualPaneLayout = ShortcutHelperDualPaneLayout.BottomSheet,
                )
            },
            maxWidth = ShortcutHelperBottomSheet.LargeScreenWidthLandscape,
            context = displayContext,
        )

    private fun createSidebarDialog(widthDp: Int): Dialog =
        dialogFactory.create(
            context = displayContext,
            dialogDelegate = SidebarDialogDelegate(widthDp),
        ) { dialog ->
            ShortcutHelperContent(
                dialog = dialog,
                modifier =
                    Modifier.fillMaxWidth().thenIf(hasExpandedWindowHeight()) {
                        Modifier.fillMaxHeight(0.83f)
                    },
                dualPaneLayout = ShortcutHelperDualPaneLayout.Dialog,
            )
        }

    @Composable
    private fun ShortcutHelperContent(
        dialog: Dialog,
        modifier: Modifier,
        dualPaneLayout: ShortcutHelperDualPaneLayout,
    ) {
        val shortcutsUiState by
            shortcutHelperViewModel.shortcutsUiState.collectAsStateWithLifecycle()
        val shortcutCustomizationDialogStarter =
            rememberActivated(traceName = "shortcutCustomizationDialogStarter") {
                shortcutCustomizationDialogStarterFactory.create(displayContext)
            }
        SelectedUserAwareInputConnection(selectedUserId = userTracker.userId) {
            ShortcutHelper(
                modifier = modifier,
                shortcutsUiState = shortcutsUiState,
                onKeyboardSettingsClicked = { onKeyboardSettingsClicked(dialog) },
                onSearchQueryChanged = { shortcutHelperViewModel.onSearchQueryChanged(it) },
                onShortcutCustomizationRequested = {
                    shortcutCustomizationDialogStarter.onShortcutCustomizationRequested(it)
                },
                onCustomizationModeToggled = { isCustomizing ->
                    shortcutHelperViewModel.toggleCustomizationMode(isCustomizing)
                },
                dualPaneLayout = dualPaneLayout,
                onCloseClicked = { dialog.dismiss() },
            )
        }
        dialog.setOnDismissListener { shortcutHelperViewModel.onViewClosed() }
        dialog.setTitle(stringResource(R.string.shortcut_helper_title))
    }

    private fun onKeyboardSettingsClicked(dialog: Dialog) {
        try {
            activityStarter.startActivity(
                Intent(Settings.ACTION_HARD_KEYBOARD_SETTINGS).addFlags(FLAG_ACTIVITY_NEW_TASK),
                /* dismissShade= */ true,
                /* animationController = */ null,
                /* showOverLockscreenWhenLocked = */ false,
                UserHandle.CURRENT,
            )
        } catch (e: ActivityNotFoundException) {
            // From the Settings docs: In some cases, a matching Activity may not exist, so ensure
            // you safeguard against this.
            e.printStackTrace()
            return
        }
        dialog.dismiss()
    }
}

/** Sizes the shortcut helper dialog and keeps it horizontally centered at the bottom. */
private class SidebarDialogDelegate(private val widthDp: Int) : DialogDelegate<SystemUIDialog> {
    override fun onCreate(dialog: SystemUIDialog, savedInstanceState: Bundle?) {
        dialog.window?.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
    }

    override fun getWidth(dialog: SystemUIDialog): Int =
        SystemUIDialog.calculateDialogWidthWithInsets(dialog, widthDp)

    override fun getHeight(dialog: SystemUIDialog): Int = ViewGroup.LayoutParams.WRAP_CONTENT

    // The taskbar stays usable while the shortcuts are listed.
    override fun shouldStashTaskbar(dialog: SystemUIDialog): Boolean = false
}
