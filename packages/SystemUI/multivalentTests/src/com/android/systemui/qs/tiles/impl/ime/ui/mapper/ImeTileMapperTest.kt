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

package com.android.systemui.qs.tiles.impl.ime.ui.mapper

import android.graphics.drawable.TestStubDrawable
import android.widget.Button
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.android.systemui.SysuiTestCase
import com.android.systemui.common.shared.model.Icon
import com.android.systemui.qs.tiles.base.shared.model.QSTileState
import com.android.systemui.qs.tiles.base.ui.model.QSTileStateSubject
import com.android.systemui.qs.tiles.impl.ime.domain.model.ImeTileModel
import com.android.systemui.qs.tiles.impl.ime.qsImeTileConfig
import com.android.systemui.res.R
import com.android.systemui.testKosmos
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class ImeTileMapperTest : SysuiTestCase() {
    private val kosmos = testKosmos()
    private val qsTileConfig = kosmos.qsImeTileConfig

    private val mapper by lazy {
        ImeTileMapper(
            context.orCreateTestableResources
                .apply { addOverride(R.drawable.ic_qs_ime, TestStubDrawable()) }
                .resources,
            context.theme,
        )
    }

    @Test
    fun mappedStateMatchesModel() {
        val outputState = mapper.map(qsTileConfig, ImeTileModel)

        QSTileStateSubject.assertThat(outputState).isEqualTo(createImeTileState())
    }

    private fun createImeTileState(): QSTileState =
        QSTileState(
            Icon.Loaded(context.getDrawable(R.drawable.ic_qs_ime)!!, null, R.drawable.ic_qs_ime),
            context.getString(R.string.quick_settings_ime_label),
            QSTileState.ActivationState.INACTIVE,
            /* secondaryLabel= */ null,
            setOf(QSTileState.UserAction.CLICK, QSTileState.UserAction.LONG_CLICK),
            context.getString(R.string.quick_settings_ime_label),
            /* stateDescription= */ null,
            QSTileState.SideViewIcon.Chevron,
            QSTileState.EnabledState.ENABLED,
            Button::class.qualifiedName,
        )
}
