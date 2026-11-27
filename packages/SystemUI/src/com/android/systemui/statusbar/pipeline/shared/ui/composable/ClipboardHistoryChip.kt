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

package com.android.systemui.statusbar.pipeline.shared.ui.composable

import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.IBinder
import android.os.UserHandle
import android.os.UserManager
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.android.systemui.common.shared.model.Icon
import com.android.systemui.compose.modifiers.sysuiResTag
import com.android.systemui.res.R
import com.android.systemui.shade.ui.composable.ChipHighlightModel
import com.android.systemui.shade.ui.composable.ShadeHighlightChip
import com.android.systemui.statusbar.phone.domain.interactor.IsAreaDark
import com.android.systemui.statusbar.shared.ui.compose.StatusBarIcon

/** PcClipboard: the history of what the person copied, and its panel. */
private object ClipboardHistory {
    const val PACKAGE = "org.lineageos.pcclipboard"
    val SERVICE = ComponentName(PACKAGE, "$PACKAGE.ClipboardHistoryService")
    val PANEL = ComponentName(PACKAGE, "$PACKAGE.PanelActivity")
}

/**
 * The clipboard history button, left of the status icons. While it shows, the app's history service
 * runs bound in the person's user, so every copy is kept; a click opens the history panel on this
 * display. Hidden when the app is not there, and on the login screen (the headless system user).
 */
@Composable
fun ClipboardHistoryChip(areaDark: IsAreaDark, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val userId = rememberCurrentUser(context)
    if (!isHistoryAvailable(context, userId)) return
    KeepHistoryRunning(context, userId)

    WithAdaptiveTint(
        highlightModel = ChipHighlightModel.Transparent,
        isDarkProvider = areaDark::isDarkTheme,
    ) { tint ->
        val description = stringResource(R.string.accessibility_clipboard_history)
        ShadeHighlightChip(
            modifier =
                modifier
                    .height(DesktopStatusBar.Dimensions.ChipHeight)
                    .widthIn(min = DesktopStatusBar.Dimensions.ChipHeight)
                    .semantics { contentDescription = description }
                    .sysuiResTag("clipboardHistory"),
            onClick = { openPanel(context, userId) },
            hoverBackgroundColor = tint.copy(alpha = ChipHighlightModel.Companion.Alpha.TRANSPARENT_HOVER),
            rippleColor = tint.copy(alpha = ChipHighlightModel.Companion.Alpha.TRANSPARENT_RIPPLE),
        ) {
            StatusBarIcon(
                icon = Icon.Resource(resId = R.drawable.ic_clipboard_history, contentDescription = null),
                tint = tint,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }
    }
}

/** The user on screen, updated when another one logs in. */
@Composable
private fun rememberCurrentUser(context: Context): Int {
    var userId by remember { mutableIntStateOf(ActivityManager.getCurrentUser()) }
    DisposableEffect(context) {
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    userId = ActivityManager.getCurrentUser()
                }
            }
        context.registerReceiver(
            receiver,
            IntentFilter(Intent.ACTION_USER_SWITCHED),
            Context.RECEIVER_NOT_EXPORTED,
        )
        onDispose { context.unregisterReceiver(receiver) }
    }
    return userId
}

private fun isHistoryAvailable(context: Context, userId: Int): Boolean {
    if (UserManager.isHeadlessSystemUserMode() && userId == UserHandle.USER_SYSTEM) return false
    val intent = Intent().setComponent(ClipboardHistory.SERVICE)
    return context.packageManager.resolveServiceAsUser(intent, 0, userId) != null
}

@Composable
private fun KeepHistoryRunning(context: Context, userId: Int) {
    DisposableEffect(context, userId) {
        val connection =
            object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {}

                override fun onServiceDisconnected(name: ComponentName) {}
            }
        val isBound =
            context.bindServiceAsUser(
                Intent().setComponent(ClipboardHistory.SERVICE),
                connection,
                Context.BIND_AUTO_CREATE,
                UserHandle.of(userId),
            )
        onDispose { if (isBound) context.unbindService(connection) }
    }
}

private fun openPanel(context: Context, userId: Int) {
    val intent =
        Intent()
            .setComponent(ClipboardHistory.PANEL)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val options = ActivityOptions.makeBasic().setLaunchDisplayId(context.displayId)
    context.startActivityAsUser(intent, options.toBundle(), UserHandle.of(userId))
}
