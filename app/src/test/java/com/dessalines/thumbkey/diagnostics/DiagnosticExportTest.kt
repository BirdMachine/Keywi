package com.dessalines.thumbkey.diagnostics

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DiagnosticExportTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun reportLargerThanBinderLimitIsExportedWithoutTruncation() {
        val report = "Keywi crash report\n" + "\tat example.long.StackTrace(🙂.kt:123)\n".repeat(80_000)
        val directory = File(temporaryFolder.root, "diagnostic-exports")
        val file = writeDiagnosticExport(directory, "crash-123", report)
        assertEquals(report, file.readText(Charsets.UTF_8))
        assertEquals("txt", file.extension)
    }

    @Test
    fun subsequentExportsDoNotOverwriteFilesAlreadyShared() {
        val first = writeDiagnosticExport(temporaryFolder.root, "crash-123", "first report")
        val second = writeDiagnosticExport(temporaryFolder.root, "crash-123", "second report")
        assertNotEquals(first, second)
        assertEquals("first report", first.readText())
        assertEquals("second report", second.readText())
    }
}
