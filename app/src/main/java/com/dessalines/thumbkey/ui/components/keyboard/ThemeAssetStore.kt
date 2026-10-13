package com.dessalines.thumbkey.ui.components.keyboard

import android.content.Context
import android.net.Uri
import java.io.File
import java.security.MessageDigest

/** Immutable, content-addressed files owned by an individual theme. */
object ThemeAssetStore {
    const val MAX_ASSET_BYTES = 64L * 1024 * 1024
    const val MAX_PACKAGE_BYTES = 128L * 1024 * 1024

    fun directory(context: Context, id: String): File {
        require(id.matches(Regex("[A-Za-z0-9_-]{1,100}"))) { "Invalid theme ID" }
        return File(context.filesDir, "themes/$id")
    }

    fun assetFile(directory: File, reference: String): File {
        require(reference.matches(Regex("assets/[A-Za-z0-9_.-]+"))) { "Invalid theme asset path" }
        val file = File(directory, reference).canonicalFile
        require(file.parentFile == File(directory, "assets").canonicalFile) { "Invalid theme asset path" }
        return file
    }

    fun resolve(context: Context, id: String, reference: String?): String? =
        reference?.let {
            if (it.startsWith("assets/")) Uri.fromFile(assetFile(directory(context, id), it)).toString() else it
        }

    fun copySelected(context: Context, uri: Uri, extension: String? = null): String {
        val reference = copy(context, CUSTOM_THEME_ID, uri.toString(), CUSTOM_THEME_ID, extension = extension)
        return requireNotNull(resolve(context, CUSTOM_THEME_ID, reference))
    }

    fun copy(
        context: Context,
        destinationId: String,
        reference: String,
        sourceId: String,
        sourceDirectory: File? = null,
        extension: String? = null,
    ): String {
        val sourceUri = if (reference.startsWith("assets/")) {
            Uri.fromFile(assetFile(sourceDirectory ?: directory(context, sourceId), reference))
        } else {
            require(sourceDirectory == null) { "Theme packages must contain their assets" }
            Uri.parse(reference)
        }
        val assets = File(directory(context, destinationId), "assets").apply { check(mkdirs() || isDirectory) }
        val suffix = (extension ?: sourceUri.lastPathSegment?.substringAfterLast('.', "") ?: "")
            .lowercase().takeIf { it.matches(Regex("[a-z0-9]{1,8}")) } ?: "bin"
        if (sourceUri.scheme == "file") {
            val sourceFile = File(requireNotNull(sourceUri.path)).canonicalFile
            if (sourceFile.parentFile == assets.canonicalFile && sourceFile.isFile) return "assets/${sourceFile.name}"
        }
        val temporary = File.createTempFile("asset-", ".tmp", assets)
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val input = when (sourceUri.scheme) {
                "file" -> {
                    val file = File(requireNotNull(sourceUri.path)).canonicalFile
                    // Legacy private files and picker copies are supported, not arbitrary private data.
                    val allowed = listOf(File(context.filesDir, "themes"), File(context.filesDir, "keyboard-fonts"), context.cacheDir)
                    require(allowed.any { file.path.startsWith(it.canonicalPath + File.separator) }) { "Asset is outside Keywi storage; select it with the file picker" }
                    file.inputStream()
                }
                "content" -> requireNotNull(context.contentResolver.openInputStream(sourceUri)) { "Cannot read theme asset" }
                else -> error("Unsupported theme asset reference")
            }
            input.use { stream ->
                temporary.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val count = stream.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= MAX_ASSET_BYTES) { "Theme asset exceeds 64 MB" }
                        digest.update(buffer, 0, count)
                        output.write(buffer, 0, count)
                    }
                    require(total > 0) { "Theme asset is empty" }
                }
            }
            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            val target = File(assets, "$hash.$suffix")
            if (!target.exists()) check(temporary.renameTo(target)) { "Could not save theme asset" }
            return "assets/${target.name}"
        } finally {
            temporary.delete()
        }
    }

    fun readable(context: Context, id: String, reference: String): Boolean = runCatching {
        val uri = Uri.parse(resolve(context, id, reference))
        if (uri.scheme == "file") File(requireNotNull(uri.path)).isFile
        else if (uri.scheme == "content") context.contentResolver.openInputStream(uri)?.use { true } ?: false
        else false
    }.getOrDefault(false)
}
