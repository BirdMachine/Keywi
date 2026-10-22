package com.dessalines.thumbkey.effects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.UUID

enum class TaskerCommandDisplayKind { TEXT, ICON, IMAGE }

/** Labels are display data only and never become key actions or intent parameters. */
data class TaskerCommandDisplay(
    val kind: TaskerCommandDisplayKind,
    val value: String,
) {
    fun valid(): Boolean =
        when (kind) {
            TaskerCommandDisplayKind.TEXT -> {
                value.isNotBlank() && value.codePointCount(0, value.length) <= 16 &&
                    value.none { it.isISOControl() }
            }

            TaskerCommandDisplayKind.ICON -> {
                value in TaskerCommandImages.icons
            }

            TaskerCommandDisplayKind.IMAGE -> {
                TaskerCommandImages.imageName.matches(value)
            }
        }

    fun toJson(): JSONObject = JSONObject().put("kind", kind.name).put("value", value)

    companion object {
        fun fromJson(json: JSONObject?): TaskerCommandDisplay? =
            runCatching {
                if (json == null) return null
                TaskerCommandDisplay(TaskerCommandDisplayKind.valueOf(json.getString("kind")), json.getString("value"))
                    .takeIf { it.valid() }
            }.getOrNull()
    }
}

/** Imports are copied, bounded before decode, sampled, and stored as small app-owned PNGs. */
object TaskerCommandImages {
    val icons =
        linkedMapOf(
            "settings" to "Gear",
            "bolt" to "Bolt",
            "play" to "Play",
            "home" to "Home",
            "favorite" to "Heart",
            "code" to "Code",
        )
    internal val imageName = Regex("[0-9a-f-]{36}\\.png")

    private fun directory(context: Context) = File(context.filesDir, "tasker-command-images")

    internal fun file(
        context: Context,
        name: String,
    ): File? = name.takeIf { imageName.matches(it) }?.let { File(directory(context), it) }

    suspend fun importImage(
        context: Context,
        uri: Uri,
    ): TaskerCommandDisplay =
        withContext(Dispatchers.IO) {
            val bytes =
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val buffer = java.io.ByteArrayOutputStream()
                    val chunk = ByteArray(8192)
                    var total = 0
                    while (true) {
                        val count = input.read(chunk)
                        if (count < 0) break
                        total += count
                        require(total <= 4 * 1024 * 1024) { "Choose an image smaller than 4 MB." }
                        buffer.write(chunk, 0, count)
                    }
                    buffer.toByteArray()
                } ?: error("Unable to open this image.")
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            require(
                bounds.outWidth in 1..8192 && bounds.outHeight in 1..8192 &&
                    bounds.outWidth.toLong() * bounds.outHeight <= 16_777_216,
            ) { "Choose a valid bitmap no larger than 16 megapixels." }
            val options =
                BitmapFactory.Options().apply {
                    inSampleSize = 1
                    while (maxOf(bounds.outWidth, bounds.outHeight) / inSampleSize > 256) inSampleSize *= 2
                }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: error("This image cannot be decoded.")
            val name = "${UUID.randomUUID()}.png"
            val target = file(context, name) ?: error("Invalid image name")
            try {
                directory(context).mkdirs()
                target.outputStream().use { require(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) { "Unable to save image." } }
                TaskerCommandDisplay(TaskerCommandDisplayKind.IMAGE, name)
            } catch (failure: Exception) {
                target.delete()
                throw failure
            } finally {
                bitmap.recycle()
            }
        }

    internal fun load(
        context: Context,
        name: String,
    ): Bitmap? {
        val target = file(context, name) ?: return null
        // Defense in depth for restored/corrupt files: never decode unbounded app data.
        if (!target.isFile || target.length() > 1024 * 1024) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(target.path, bounds)
        if (bounds.outWidth !in 1..256 || bounds.outHeight !in 1..256) return null
        return BitmapFactory.decodeFile(target.path)
    }

    internal fun delete(
        context: Context,
        name: String,
    ) {
        file(context, name)?.delete()
    }
}
