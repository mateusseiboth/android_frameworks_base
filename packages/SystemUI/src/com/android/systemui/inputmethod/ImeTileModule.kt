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

package com.android.systemui.inputmethod

import com.android.systemui.qs.QsEventLogger
import com.android.systemui.qs.pipeline.shared.TileSpec
import com.android.systemui.qs.shared.model.TileCategory
import com.android.systemui.qs.tileimpl.QSTileImpl
import com.android.systemui.qs.tiles.ImeTile
import com.android.systemui.qs.tiles.base.domain.interactor.QSTileAvailabilityInteractor
import com.android.systemui.qs.tiles.base.shared.model.QSTileConfig
import com.android.systemui.qs.tiles.base.shared.model.QSTileUIConfig
import com.android.systemui.qs.tiles.base.ui.viewmodel.QSTileViewModel
import com.android.systemui.qs.tiles.base.ui.viewmodel.QSTileViewModelFactory
import com.android.systemui.qs.tiles.impl.ime.domain.interactor.ImeTileDataInteractor
import com.android.systemui.qs.tiles.impl.ime.domain.interactor.ImeTileUserActionInteractor
import com.android.systemui.qs.tiles.impl.ime.domain.model.ImeTileModel
import com.android.systemui.qs.tiles.impl.ime.ui.mapper.ImeTileMapper
import com.android.systemui.res.R
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/** Quick settings tile that opens the input method picker. */
@Module
interface ImeTileModule {

    /** Inject ImeTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(ImeTile.TILE_SPEC)
    fun bindImeTile(imeTile: ImeTile): QSTileImpl<*>

    @Binds
    @IntoMap
    @StringKey(IME_TILE_SPEC)
    fun provideImeAvailabilityInteractor(impl: ImeTileDataInteractor): QSTileAvailabilityInteractor

    companion object {
        const val IME_TILE_SPEC = "ime"

        @Provides
        @IntoMap
        @StringKey(IME_TILE_SPEC)
        fun provideImeTileConfig(uiEventLogger: QsEventLogger): QSTileConfig =
            QSTileConfig(
                tileSpec = TileSpec.create(IME_TILE_SPEC),
                uiConfig =
                    QSTileUIConfig.Resource(
                        iconRes = R.drawable.ic_qs_ime,
                        labelRes = R.string.quick_settings_ime_label,
                    ),
                instanceId = uiEventLogger.getNewInstanceId(),
                category = TileCategory.UTILITIES,
            )

        /** Inject ImeTile into tileViewModelMap in QSModule */
        @Provides
        @IntoMap
        @StringKey(IME_TILE_SPEC)
        fun provideImeTileViewModel(
            factory: QSTileViewModelFactory.Static<ImeTileModel>,
            mapper: ImeTileMapper,
            stateInteractor: ImeTileDataInteractor,
            userActionInteractor: ImeTileUserActionInteractor,
        ): QSTileViewModel =
            factory.create(
                TileSpec.create(IME_TILE_SPEC),
                userActionInteractor,
                stateInteractor,
                mapper,
            )
    }
}
