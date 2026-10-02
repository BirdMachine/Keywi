package com.dessalines.thumbkey.diagnostics

import java.io.File

/** Export complete UTF-8 reports as files, keeping large text out of Android's Binder messages. */
internal fun writeDiagnosticExport(directory: File, prefix: String, contents: String): File {
    require(prefix.isNotBlank() && prefix == File(prefix).name) { "Invalid export filename" }
    check(directory.isDirectory || directory.mkdirs()) { "Couldn't create export directory" }
    return File.createTempFile("$prefix-", ".txt", directory).apply {
        writeText(contents, Charsets.UTF_8)
    }
}
