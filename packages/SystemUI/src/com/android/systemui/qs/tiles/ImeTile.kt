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

package com.android.systemui.qs.tiles

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import com.android.internal.logging.MetricsLogger
import com.android.systemui.animation.Expandable
import com.android.systemui.common.shared.model.Icon
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.dagger.qualifiers.Main
import com.android.systemui.plugins.ActivityStarter
import com.android.systemui.plugins.FalsingManager
import com.android.systemui.plugins.qs.QSTile
import com.android.systemui.plugins.statusbar.StatusBarStateController
import com.android.systemui.qs.QSHost
import com.android.systemui.qs.QsEventLogger
import com.android.systemui.qs.logging.QSLogger
import com.android.systemui.qs.tileimpl.QSTileImpl
import com.android.systemui.qs.tiles.base.shared.model.QSTileConfigProvider
import com.android.systemui.qs.tiles.impl.ime.domain.interactor.ImeTileDataInteractor
import com.android.systemui.qs.tiles.impl.ime.domain.interactor.ImeTileUserActionInteractor
import com.android.systemui.qs.tiles.impl.ime.domain.model.ImeTileModel
import com.android.systemui.qs.tiles.impl.ime.ui.mapper.ImeTileMapper
import com.android.systemui.res.R
import javax.inject.Inject

/**
 * Quick settings tile: input method. Opens the input method picker to switch the keyboard and its
 * layout. Delegates to the new tile architecture, which is used when qs_new_tiles is on.
 */
class ImeTile
@Inject
constructor(
    host: QSHost,
    uiEventLogger: QsEventLogger,
    @Background backgroundLooper: Looper,
    @Main mainHandler: Handler,
    falsingManager: FalsingManager,
    metricsLogger: MetricsLogger,
    statusBarStateController: StatusBarStateController,
    activityStarter: ActivityStarter,
    qsLogger: QSLogger,
    qsTileConfigProvider: QSTileConfigProvider,
    private val dataInteractor: ImeTileDataInteractor,
    private val tileMapper: ImeTileMapper,
    private val userActionInteractor: ImeTileUserActionInteractor,
) :
    QSTileImpl<QSTile.State?>(
        host,
        uiEventLogger,
        backgroundLooper,
        mainHandler,
        falsingManager,
        metricsLogger,
        statusBarStateController,
        activityStarter,
        qsLogger,
    ) {

    private val config = qsTileConfigProvider.getConfig(TILE_SPEC)

    override fun getTileLabel(): CharSequence = mContext.getString(config.uiConfig.labelRes)

    override fun newTileState(): QSTile.State? =
        QSTile.State().apply { state = Tile.STATE_INACTIVE }

    override fun handleClick(expandable: Expandable?) {
        userActionInteractor.handleClick()
    }

    override fun getLongClickIntent(): Intent = userActionInteractor.longClickIntent

    override fun handleUpdateState(state: QSTile.State?, arg: Any?) {
        val model = arg as? ImeTileModel ?: dataInteractor.getCurrentTileModel()
        val tileState = tileMapper.map(config, model)

        state?.apply {
            this.state = tileState.activationState.legacyState
            icon =
                maybeLoadResourceIcon((tileState.icon as Icon.Loaded).resId ?: R.drawable.ic_qs_ime)
            label = tileState.label
            contentDescription = tileState.contentDescription
            expandedAccessibilityClassName = tileState.expandedAccessibilityClassName
        }
    }

    override fun isAvailable(): Boolean = dataInteractor.isAvailable()

    companion object {
        const val TILE_SPEC = "ime"
    }
}
