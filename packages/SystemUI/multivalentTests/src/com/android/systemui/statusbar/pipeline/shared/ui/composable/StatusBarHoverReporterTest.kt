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

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StatusBarHoverReporterTest {

    private val reporter = StatusBarHoverReporter(thresholdPx = 8f)

    @Test
    fun firstHover_reports() {
        assertTrue(reporter.shouldReportMove(x = 100f, y = 10f))
    }

    @Test
    fun smallMove_doesNotReport() {
        reporter.shouldReportMove(x = 100f, y = 10f)

        assertFalse(reporter.shouldReportMove(x = 105f, y = 12f))
    }

    @Test
    fun moveBeyondThreshold_reports() {
        reporter.shouldReportMove(x = 100f, y = 10f)

        assertTrue(reporter.shouldReportMove(x = 109f, y = 10f))
    }

    @Test
    fun smallMoves_addUpFromTheLastReport() {
        reporter.shouldReportMove(x = 100f, y = 10f)
        reporter.shouldReportMove(x = 105f, y = 10f)

        assertTrue(reporter.shouldReportMove(x = 110f, y = 10f))
    }

    @Test
    fun exitAfterHover_reports() {
        reporter.shouldReportMove(x = 100f, y = 10f)

        assertTrue(reporter.shouldReportExit())
    }

    @Test
    fun exitWithoutHover_doesNotReport() {
        assertFalse(reporter.shouldReportExit())
    }

    @Test
    fun hoverAfterExit_reportsAgain() {
        reporter.shouldReportMove(x = 100f, y = 10f)
        reporter.shouldReportExit()

        assertTrue(reporter.shouldReportMove(x = 101f, y = 10f))
    }
}
