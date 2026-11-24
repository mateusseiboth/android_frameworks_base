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

package com.android.server.wm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for {@link MouseEdgeRevealDelay}.
 *
 * Build/Install/Run:
 *  atest WmTests:MouseEdgeRevealDelayTest
 */
public class MouseEdgeRevealDelayTest {

    @Test
    public void fromConfig_configuredDelay_isUsed() {
        assertEquals(200L, MouseEdgeRevealDelay.fromConfig(200));
    }

    @Test
    public void fromConfig_zero_revealsAtOnce() {
        assertEquals(0L, MouseEdgeRevealDelay.fromConfig(0));
    }

    @Test
    public void fromConfig_negative_fallsBackToTheDefault() {
        assertEquals(MouseEdgeRevealDelay.DEFAULT_DELAY_MS, MouseEdgeRevealDelay.fromConfig(-1));
    }

    @Test
    public void defaultDelay_is500Ms() {
        assertEquals(500L, MouseEdgeRevealDelay.DEFAULT_DELAY_MS);
    }
}
