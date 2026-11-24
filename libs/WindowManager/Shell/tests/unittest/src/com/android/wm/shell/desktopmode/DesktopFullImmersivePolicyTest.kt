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

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for [DesktopFullImmersivePolicy]. */
class DesktopFullImmersivePolicyTest {

    @Test
    fun canEnterFullImmersive_whenAllowed_isTrue() {
        assertTrue(DesktopFullImmersivePolicy(isFullImmersiveDisabled = false).canEnter())
    }

    @Test
    fun canEnterFullImmersive_whenDisabled_isFalse() {
        assertFalse(DesktopFullImmersivePolicy(isFullImmersiveDisabled = true).canEnter())
    }

    @Test
    fun shouldOfferImmersiveOption_appRequestingImmersive_whenAllowed_isTrue() {
        val policy = DesktopFullImmersivePolicy(isFullImmersiveDisabled = false)

        assertTrue(policy.shouldOfferImmersiveOption(requestingImmersive = true))
    }

    @Test
    fun shouldOfferImmersiveOption_appNotRequestingImmersive_isFalse() {
        val policy = DesktopFullImmersivePolicy(isFullImmersiveDisabled = false)

        assertFalse(policy.shouldOfferImmersiveOption(requestingImmersive = false))
    }

    @Test
    fun shouldOfferImmersiveOption_appRequestingImmersive_whenDisabled_isFalse() {
        val policy = DesktopFullImmersivePolicy(isFullImmersiveDisabled = true)

        assertFalse(policy.shouldOfferImmersiveOption(requestingImmersive = true))
    }
}
