package com.dessalines.thumbkey.diagnostics

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.dessalines.thumbkey.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Privacy-first, local-only diagnostics. Callers must never log typed text. */
object KeywiDiagnostics {
    private const val TAG = "KeywiDiag"
    private const val DIR = "diagnostics"
    private const val LOG = "keywi.log"
    private const val MAX_LOG_BYTES = 2L * 1024L * 1024L
    private const val KEEP_CRASHES = 20
    private const val INPUT_SAMPLE_SIZE = 25
    private val lock = Any()
    private var appContext: Context? = null
    private var previousHandler: Thread.UncaughtExceptionHandler? = null
    private var inputCount = 0
    private var inputWindowStarted = 0L
    private var lastInputAt = 0L
    private var maxInputGap = 0L

    fun install(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try { recordCrash(thread, throwable) } catch (_: Throwable) { } finally {
                previousHandler?.uncaughtException(thread, throwable)
            }
        }
        event("APP", "diagnostics initialized")
    }

    fun event(category: String, message: String) = write("I", category, message, null)
    fun warning(category: String, message: String) = write("W", category, message, null)
    fun error(category: String, message: String, throwable: Throwable? = null) = write("E", category, message, throwable)

    /**
     * Records aggregate input timing without recording keys or text. Deliberately writes only once
     * per sample window so diagnostics do not themselves create per-keystroke I/O or stutter.
     */
    fun inputPulse() {
        val now = SystemClock.uptimeMillis()
        var summary: String? = null
        synchronized(lock) {
            if (inputWindowStarted == 0L) inputWindowStarted = now
            if (lastInputAt != 0L) maxInputGap = maxOf(maxInputGap, now - lastInputAt)
            lastInputAt = now
            inputCount++
            if (inputCount >= INPUT_SAMPLE_SIZE) {
                val elapsed = (now - inputWindowStarted).coerceAtLeast(1L)
                summary = "25 input actions in ${elapsed}ms; max inter-action gap=${maxInputGap}ms"
                inputCount = 0
                inputWindowStarted = now
                maxInputGap = 0L
            }
        }
        summary?.let { event("PERF_INPUT", it) }
    }

    fun performance(category: String, operation: String, elapsedMs: Long, warnAtMs: Long = 32L) {
        if (elapsedMs >= warnAtMs) warning(category, "$operation took ${elapsedMs}ms")
    }

    private fun write(level: String, category: String, message: String, throwable: Throwable?) {
        val context = appContext ?: return
        val safeCategory = category.take(24).replace(Regex("[^A-Za-z0-9_-]"), "_")
        val safeMessage = message.replace('\n', ' ').take(1500)
        val line = buildString {
            append(timestamp()).append(' ').append(level).append('/').append(safeCategory).append("  ").append(safeMessage)
            throwable?.let { append(" | ").append(Log.getStackTraceString(it).take(12000).replace('\n', ' ')) }
            append('\n')
        }
        synchronized(lock) {
            val file = logFile(context)
            rotateIfNeeded(file, line.length)
            file.appendText(line)
        }
        when (level) {
            "E" -> Log.e(TAG, "[$safeCategory] $safeMessage", throwable)
            "W" -> Log.w(TAG, "[$safeCategory] $safeMessage")
            else -> Log.i(TAG, "[$safeCategory] $safeMessage")
        }
    }

    private fun recordCrash(thread: Thread, throwable: Throwable) {
        val context = appContext ?: return
        val file = File(diagnosticDir(context), "crash-${System.currentTimeMillis()}.txt")
        file.writeText(buildString {
            appendLine("Keywi crash report")
            appendLine("Time: ${timestamp()}")
            appendLine("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("Android: ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Thread: ${thread.name}")
            appendLine()
            appendLine(Log.getStackTraceString(throwable))
            appendLine("--- recent Keywi events ---")
            append(readLog(context).takeLast(64_000))
        })
        trimCrashReports(context)
    }

    fun readLog(context: Context): String = runCatching { logFile(context).takeIf(File::exists)?.readText().orEmpty() }.getOrDefault("")

    fun crashReports(context: Context): List<File> =
        diagnosticDir(context).listFiles { file -> file.name.startsWith("crash-") && file.extension == "txt" }
            ?.sortedByDescending(File::lastModified).orEmpty()

    fun clear(context: Context) {
        synchronized(lock) { diagnosticDir(context).listFiles()?.forEach { it.delete() } }
        event("APP", "diagnostic history cleared")
    }

    fun systemExitHistory(context: Context): List<String> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return emptyList()
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return runCatching {
            am.getHistoricalProcessExitReasons(context.packageName, 0, 12).map { info ->
                "${formatTime(info.timestamp)} — ${reasonName(info.reason)} (importance ${info.importance})"
            }
        }.getOrDefault(emptyList())
    }

    fun buildReport(context: Context): String = buildString {
        appendLine("KEYWI DIAGNOSTIC REPORT")
        appendLine("Generated: ${timestamp()}")
        appendLine("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("Android: ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}")
        appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine()
        appendLine("=== PROCESS EXIT HISTORY ===")
        systemExitHistory(context).forEach(::appendLine)
        appendLine()
        appendLine("=== CRASH REPORTS ===")
        crashReports(context).forEach { file ->
            appendLine("--- ${file.name} ---")
            appendLine(runCatching { file.readText() }.getOrDefault("<unreadable>"))
        }
        appendLine()
        appendLine("=== KEYWI EVENT LOG ===")
        append(readLog(context))
        appendLine()
        appendLine("Privacy note: Keywi diagnostics record app actions/timing, never typed text.")
    }

    private fun diagnosticDir(context: Context) = File(context.filesDir, DIR).apply { mkdirs() }
    private fun logFile(context: Context) = File(diagnosticDir(context), LOG)

    private fun rotateIfNeeded(file: File, incomingChars: Int) {
        if (file.exists() && file.length() + incomingChars > MAX_LOG_BYTES) {
            val text = runCatching { file.readText() }.getOrDefault("")
            file.writeText(text.takeLast((MAX_LOG_BYTES / 2).toInt()))
        }
    }

    private fun trimCrashReports(context: Context) { crashReports(context).drop(KEEP_CRASHES).forEach { it.delete() } }
    private fun timestamp() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
    private fun formatTime(value: Long) = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(value))

    private fun reasonName(reason: Int): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        when (reason) {
            ApplicationExitInfo.REASON_CRASH -> "CRASH"
            ApplicationExitInfo.REASON_CRASH_NATIVE -> "NATIVE CRASH"
            ApplicationExitInfo.REASON_ANR -> "ANR"
            ApplicationExitInfo.REASON_LOW_MEMORY -> "LOW MEMORY"
            ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> "EXCESSIVE RESOURCE USE"
            ApplicationExitInfo.REASON_USER_REQUESTED -> "USER REQUESTED"
            ApplicationExitInfo.REASON_SIGNALED -> "SIGNAL"
            ApplicationExitInfo.REASON_INITIALIZATION_FAILURE -> "INIT FAILURE"
            ApplicationExitInfo.REASON_PERMISSION_CHANGE -> "PERMISSION CHANGE"
            ApplicationExitInfo.REASON_PACKAGE_STATE_CHANGE -> "PACKAGE STATE CHANGE"
            ApplicationExitInfo.REASON_PACKAGE_UPDATED -> "PACKAGE UPDATED"
            ApplicationExitInfo.REASON_DEPENDENCY_DIED -> "DEPENDENCY DIED"
            ApplicationExitInfo.REASON_OTHER -> "OTHER"
            else -> "reason=$reason"
        }
    } else "reason=$reason"
}
