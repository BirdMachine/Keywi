package com.dessalines.thumbkey.ui.components.keyboard.shader

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.SystemClock
import android.util.Log
import android.view.View
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Opt-in, translucent GLSL ES 2.0 single-pass renderer. Never receives keyboard text.
 * Standard Shadertoy mainImage + iResolution/iTime/iFrame/iMouse are supplied.
 * Does not yet support iChannels, buffer passes or external Shadertoy imports.
 */
class KeywiShaderView(
    context: Context,
    fragmentSource: String,
    private val fps: Int = 24,
) : GLSurfaceView(context) {
    private val renderer = ShaderRenderer(fragmentSource)

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT)
        setZOrderOnTop(false)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        isClickable = false
        isFocusable = false
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    private val repaint =
        object : Runnable {
            override fun run() {
                if (visibility == VISIBLE && isAttachedToWindow) {
                    requestRender()
                    postDelayed(this, (1000L / fps.coerceIn(1, 30)))
                }
            }
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onResume()
        removeCallbacks(repaint)
        post(repaint)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(repaint)
        onPause()
        super.onDetachedFromWindow()
    }

    /** GLSurfaceView is visual-only and must not consume keystrokes. */
    override fun onTouchEvent(event: android.view.MotionEvent?): Boolean = false
}

internal class ShaderRenderer(private val source: String) : GLSurfaceView.Renderer {
    private var program = 0
    private var start = SystemClock.elapsedRealtime()
    private var frame = 0
    private var width = 1
    private var height = 1
    private val vertices = java.nio.ByteBuffer.allocateDirect(8 * 4)
        .order(java.nio.ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f))
            position(0)
        }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        start = SystemClock.elapsedRealtime()
        frame = 0
        program = runCatching {
            val vertex = compile(GLES20.GL_VERTEX_SHADER, """
                attribute vec2 position;
                void main() { gl_Position = vec4(position, 0.0, 1.0); }
            """.trimIndent())
            val fragment = compile(GLES20.GL_FRAGMENT_SHADER, """
                precision highp float;
                uniform vec3 iResolution;
                uniform float iTime;
                uniform int iFrame;
                uniform vec4 iMouse;
                ${source}
                void main() {
                    vec4 color = vec4(0.0);
                    mainImage(color, gl_FragCoord.xy);
                    gl_FragColor = color;
                }
            """.trimIndent())
            val p = GLES20.glCreateProgram()
            GLES20.glAttachShader(p, vertex)
            GLES20.glAttachShader(p, fragment)
            GLES20.glLinkProgram(p)
            val status = IntArray(1)
            GLES20.glGetProgramiv(p, GLES20.GL_LINK_STATUS, status, 0)
            GLES20.glDeleteShader(vertex)
            GLES20.glDeleteShader(fragment)
            if (status[0] == 0) {
                val error = GLES20.glGetProgramInfoLog(p)
                GLES20.glDeleteProgram(p)
                error("Shader link failed: $error")
            }
            p
        }.onFailure { Log.e("KeywiShader", "Shader disabled after compile failure", it) }.getOrDefault(0)
        if (program != 0) Log.i("KeywiShader", "Shader program linked")
        GLES20.glClearColor(0f, 0f, 0f, 0f)
    }

    private fun compile(type: Int, src: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, src)
        GLES20.glCompileShader(shader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val info = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            error("Shader compilation failed: $info")
        }
        return shader
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        width = w.coerceAtLeast(1)
        height = h.coerceAtLeast(1)
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        if (program == 0) return
        // Android's TextureView compositor expects premultiplied RGBA.
        // Blend the fragment onto the transparent surface with independent
        // alpha factors: SRC_ALPHA for RGB, ONE for alpha. The old shared
        // SRC_ALPHA factor squared overlay alpha (0.20 -> 0.04); disabling
        // blending would instead leave RGB non-premultiplied.
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFuncSeparate(
            GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA,
            GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA,
        )
        GLES20.glUseProgram(program)
        GLES20.glUniform3f(GLES20.glGetUniformLocation(program, "iResolution"), width.toFloat(), height.toFloat(), 1f)
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "iTime"), (SystemClock.elapsedRealtime() - start) / 1000f)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "iFrame"), frame++)
        GLES20.glUniform4f(GLES20.glGetUniformLocation(program, "iMouse"), 0f, 0f, 0f, 0f)
        val attr = GLES20.glGetAttribLocation(program, "position")
        if (attr < 0) return
        GLES20.glEnableVertexAttribArray(attr)
        vertices.position(0)
        GLES20.glVertexAttribPointer(attr, 2, GLES20.GL_FLOAT, false, 0, vertices)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(attr)
    }
}
