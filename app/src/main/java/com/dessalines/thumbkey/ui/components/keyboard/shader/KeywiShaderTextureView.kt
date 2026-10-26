package com.dessalines.thumbkey.ui.components.keyboard.shader

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Inline-composited GLES shader surface.
 *
 * Unlike GLSurfaceView (a separate SurfaceView window layer), TextureView is
 * composed into the same window as Compose. This lets translucent effects
 * respect sibling draw order and avoids placing an opaque surface over keys.
 * Experimental until emulator/device visual and touch tests pass.
 */
class KeywiShaderTextureView(
    context: Context,
    fragmentSource: String,
    fps: Int = 24,
) : TextureView(context), TextureView.SurfaceTextureListener {
    private val shaderRenderer = ShaderRenderer(fragmentSource)
    private val frameDelayMs = 1000L / fps.coerceIn(1, 30)
    private var worker: HandlerThread? = null
    private var handler: Handler? = null

    @Volatile private var active = false
    @Volatile private var visible = true

    private var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    // Written only by the GL worker; reset before a new worker is started.
    private var firstFramePresented = false

    init {
        isOpaque = false
        isClickable = false
        isFocusable = false
        isEnabled = false
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        surfaceTextureListener = this
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean = false

    private fun refreshVisibility() {
        // isShown accounts for ancestor visibility, but is only reliable after
        // attachment. A TextureView can attach already visible without receiving
        // another onVisibilityChanged callback.
        visible = isAttachedToWindow && isShown && windowVisibility == VISIBLE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        refreshVisibility()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        refreshVisibility()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        refreshVisibility()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        stopWorker()
        refreshVisibility()
        firstFramePresented = false
        val thread = HandlerThread("KeywiShaderGL").apply { start() }
        worker = thread
        val glHandler = Handler(thread.looper)
        handler = glHandler
        active = true
        glHandler.post {
            if (!active) return@post
            val ready = runCatching {
                createEgl(surface)
                shaderRenderer.onSurfaceCreated(null, null)
                shaderRenderer.onSurfaceChanged(null, width, height)
            }.onFailure { Log.e("KeywiShader", "TextureView EGL setup failed", it) }.isSuccess
            if (ready && active) {
                glHandler.post(renderFrame)
            } else {
                releaseEgl()
            }
        }
    }

    private val renderFrame = object : Runnable {
        override fun run() {
            if (!active) return
            if (visible) {
                try {
                    shaderRenderer.onDrawFrame(null)
                    if (!EGL14.eglSwapBuffers(display, eglSurface)) {
                        error("eglSwapBuffers failed: " + EGL14.eglGetError())
                    }
                    if (!firstFramePresented) {
                        firstFramePresented = true
                        // A successful swap proves GLES rendered a frame, but
                        // NOT that Compose z-order, alpha, or touch are correct.
                        Log.i("KeywiShader", "Shader first frame presented")
                    }
                } catch (error: Exception) {
                    Log.e("KeywiShader", "Stopping failed TextureView renderer", error)
                    active = false
                    releaseEgl()
                    return
                }
            }
            if (active) handler?.postDelayed(this, if (visible) frameDelayMs else 250L)
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        handler?.post {
            if (active && display != EGL14.EGL_NO_DISPLAY) {
                shaderRenderer.onSurfaceChanged(null, width, height)
            }
        }
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        stopWorker()
        // The GL worker has released its EGLSurface before TextureView releases this texture.
        return true
    }

    override fun onDetachedFromWindow() {
        stopWorker()
        super.onDetachedFromWindow()
    }

    private fun stopWorker() {
        active = false
        val glHandler = handler ?: return
        handler = null
        val thread = worker
        worker = null
        glHandler.removeCallbacksAndMessages(null)
        val done = CountDownLatch(1)
        glHandler.post {
            try {
                releaseEgl()
            } finally {
                done.countDown()
            }
        }
        if (!done.await(2, TimeUnit.SECONDS)) {
            Log.w("KeywiShader", "Timed out waiting for GL thread cleanup")
        }
        thread?.quitSafely()
    }

    /** Only called on the dedicated GL worker thread. */
    private fun createEgl(surfaceTexture: SurfaceTexture) {
        display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        check(display != EGL14.EGL_NO_DISPLAY) { "No EGL display" }
        val version = IntArray(2)
        check(EGL14.eglInitialize(display, version, 0, version, 1)) { "EGL initialize failed" }
        val attributes = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_NONE,
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val count = IntArray(1)
        check(EGL14.eglChooseConfig(display, attributes, 0, configs, 0, 1, count, 0) && count[0] > 0) {
            "No translucent GLES2 EGL config"
        }
        val config = requireNotNull(configs[0])
        eglContext = EGL14.eglCreateContext(
            display, config, EGL14.EGL_NO_CONTEXT,
            intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE), 0,
        )
        check(eglContext != EGL14.EGL_NO_CONTEXT) { "EGL context creation failed" }
        eglSurface = EGL14.eglCreateWindowSurface(
            display, config, surfaceTexture, intArrayOf(EGL14.EGL_NONE), 0,
        )
        check(eglSurface != EGL14.EGL_NO_SURFACE) { "EGL window surface creation failed" }
        check(EGL14.eglMakeCurrent(display, eglSurface, eglSurface, eglContext)) {
            "EGL make-current failed"
        }
    }

    /** Only called on the dedicated GL worker thread. */
    private fun releaseEgl() {
        if (display == EGL14.EGL_NO_DISPLAY) return
        EGL14.eglMakeCurrent(
            display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT,
        )
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglDestroySurface(display, eglSurface)
            eglSurface = EGL14.EGL_NO_SURFACE
        }
        if (eglContext != EGL14.EGL_NO_CONTEXT) {
            EGL14.eglDestroyContext(display, eglContext)
            eglContext = EGL14.EGL_NO_CONTEXT
        }
        EGL14.eglTerminate(display)
        display = EGL14.EGL_NO_DISPLAY
    }
}
