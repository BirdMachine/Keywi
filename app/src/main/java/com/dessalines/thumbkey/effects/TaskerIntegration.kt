package com.dessalines.thumbkey.effects

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import java.util.UUID

data class TaskerTask(
    val name: String,
    val packageName: String,
)

/**
 * The documented named-task subset of TaskerIntent 1.3.3.
 * Sources: https://tasker.joaoapps.com/invoketasks.html
 *          https://tasker.joaoapps.com/code/TaskerIntent.java
 * Never accepts serialized intents, arbitrary extras, task actions, or typed editor content.
 */
object TaskerIntegration {
    const val PERMISSION = "net.dinglisch.android.tasker.PERMISSION_RUN_TASKS"
    const val ACTION_SELECT = "net.dinglisch.android.tasker.ACTION_TASK_SELECT"
    const val ACTION_RUN = "net.dinglisch.android.tasker.ACTION_TASK"
    val packages = listOf("net.dinglisch.android.taskerm", "net.dinglisch.android.tasker")

    enum class Status(
        val message: String,
    ) {
        READY("Tasker is ready. Tasks run asynchronously."),
        MISSING("Install or enable Tasker to use this assignment."),
        PERMISSION(
            "Grant Keywi permission to run Tasker tasks. If Android cannot offer permission, back up Keywi before reinstalling it after Tasker.",
        ),
        DISABLED("Enable Tasker from its main screen."),
        BLOCKED("In Tasker, open Preferences → Misc and enable Allow External Access."),
        UNAVAILABLE("Tasker status could not be read. Open Tasker, enable Allow External Access, then return here."),
        INVALID("Choose an existing Tasker task. Names must contain 1–256 characters without control characters."),
    }

    fun valid(task: TaskerTask): Boolean =
        task.packageName in packages && task.name.isNotBlank() && task.name.length <= 256 && task.name.none { it.isISOControl() }

    @Suppress("DEPRECATION")
    fun installedPackage(context: Context): String? =
        packages.firstOrNull { pkg ->
            runCatching { context.packageManager.getApplicationInfo(pkg, 0).enabled }.getOrDefault(false)
        }

    fun selectionIntent(packageName: String): Intent {
        require(packageName in packages)
        return Intent(ACTION_SELECT).setPackage(packageName).setFlags(
            Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NO_HISTORY,
        )
    }

    fun externalAccessIntent(packageName: String): Intent {
        require(packageName in packages)
        return Intent("net.dinglisch.android.tasker.ACTION_OPEN_PREFS").setPackage(packageName).putExtra("tno", 3)
    }

    internal fun executionIntent(task: TaskerTask): Intent {
        require(valid(task))
        return Intent(ACTION_RUN)
            .setPackage(task.packageName)
            .setData(Uri.parse("id:${UUID.randomUUID()}"))
            .putExtra("version_number", "1.1")
            .putExtra("task_name", task.name)
    }

    @Suppress("DEPRECATION")
    fun status(
        context: Context,
        packageName: String? = installedPackage(context),
    ): Status {
        if (packageName !in packages) return Status.MISSING
        val installed = runCatching { context.packageManager.getApplicationInfo(packageName!!, 0).enabled }.getOrDefault(false)
        if (!installed) return Status.MISSING
        if (context.checkSelfPermission(PERMISSION) != PackageManager.PERMISSION_GRANTED) return Status.PERMISSION
        return try {
            context.contentResolver
                .query(
                    Uri.parse("content://net.dinglisch.android.tasker/prefs"),
                    arrayOf("enabled", "ext_access"),
                    null,
                    null,
                    null,
                )?.use { cursor ->
                    if (!cursor.moveToFirst()) return@use Status.UNAVAILABLE
                    val enabled = cursor.getColumnIndex("enabled").takeIf { it >= 0 } ?: return@use Status.UNAVAILABLE
                    val access = cursor.getColumnIndex("ext_access").takeIf { it >= 0 } ?: return@use Status.UNAVAILABLE
                    when {
                        cursor.getString(enabled) != "true" -> Status.DISABLED
                        cursor.getString(access) != "true" -> Status.BLOCKED
                        else -> Status.READY
                    }
                } ?: Status.UNAVAILABLE
        } catch (_: SecurityException) {
            Status.BLOCKED
        } catch (_: Exception) {
            Status.UNAVAILABLE
        }
    }

    /** READY means a broadcast was sent, not that the user's task completed successfully. */
    fun execute(
        context: Context,
        task: TaskerTask,
    ): Status {
        if (!valid(task)) return Status.INVALID
        val status = status(context, task.packageName)
        if (status != Status.READY) return status
        return try {
            context.sendBroadcast(executionIntent(task))
            Status.READY
        } catch (_: SecurityException) {
            Status.PERMISSION
        } catch (_: Exception) {
            Status.UNAVAILABLE
        }
    }
}
