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

package com.android.systemui.screenrecord.region

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface

/**
 * Records a region of the display: the virtual display mirrors the whole display into a texture,
 * and every frame the region is scaled onto the encoder's surface on a GL thread of its own.
 */
class CroppedRecordingSurface(
    encoderSurface: Surface,
    private val encoderSize: Size,
    displaySize: Size,
    window: TextureWindow,
) : RecordingSurface, SurfaceTexture.OnFrameAvailableListener {
    override val width = displaySize.width
    override val height = displaySize.height
    override lateinit var surface: Surface

    private val thread = HandlerThread(THREAD_NAME).apply { start() }
    private val handler = Handler(thread.looper)
    private val cropMatrix = window.toCropMatrix()
    private val texMatrix = FloatArray(16)
    private lateinit var egl: EncoderEglTarget
    private lateinit var shader: CropShader
    private lateinit var texture: SurfaceTexture
    @Volatile private var released = false

    init {
        runOnGlThread { setUp(encoderSurface) }
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        if (released) return
        texture.updateTexImage()
        texture.getTransformMatrix(texMatrix)
        shader.draw(encoderSize.width, encoderSize.height, texMatrix, cropMatrix)
        egl.swap(texture.timestamp)
    }

    override fun release() {
        if (released) return
        released = true
        runOnGlThread { tearDown() }
        thread.quitSafely()
    }

    private fun setUp(encoderSurface: Surface) {
        egl = EncoderEglTarget(encoderSurface)
        shader = CropShader()
        texture =
            SurfaceTexture(shader.textureId).apply {
                setDefaultBufferSize(width, height)
                setOnFrameAvailableListener(this@CroppedRecordingSurface, handler)
            }
        surface = Surface(texture)
    }

    private fun tearDown() {
        surface.release()
        texture.release()
        shader.release()
        egl.release()
    }

    private fun runOnGlThread(block: () -> Unit) {
        var failure: Throwable? = null
        handler.runWithScissors({ runCatching(block).onFailure { failure = it } }, TIMEOUT_MS)
        failure?.let { throw IllegalStateException("Region recording surface failed", it) }
    }

    private companion object {
        const val THREAD_NAME = "ScreenRecordRegion"
        const val TIMEOUT_MS = 2000L
    }
}
