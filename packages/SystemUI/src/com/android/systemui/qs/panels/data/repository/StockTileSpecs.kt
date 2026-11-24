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

package com.android.systemui.qs.panels.data.repository

/** Builds the list of stock tile specs from comma separated resource strings. */
object StockTileSpecs {

    /** The specs of [stock] followed by those of [extra] that [stock] does not list. */
    fun merge(stock: String, extra: String): List<String> = (split(stock) + split(extra)).distinct()

    private fun split(specs: String): List<String> =
        specs.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
