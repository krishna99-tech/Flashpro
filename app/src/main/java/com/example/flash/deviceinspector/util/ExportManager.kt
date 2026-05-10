package com.example.flash.deviceinspector.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.flash.deviceinspector.domain.model.InfoSection
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportManager {
    fun exportText(context: Context, sections: List<InfoSection>): File {
        val file = File(context.cacheDir, "DeviceInspector_${ts()}.txt")
        val content = buildString {
            sections.forEach { s ->
                appendLine("## ${s.title}")
                s.items.forEach { i -> appendLine("${i.label}: ${i.value}") }
                appendLine()
            }
        }
        file.writeText(content)
        return file
    }

    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = when {
                file.name.endsWith(".json") -> "application/json"
                file.name.endsWith(".csv") -> "text/csv"
                else -> "text/plain"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share DeviceInspector Export"))
    }

    fun exportCsv(context: Context, sections: List<InfoSection>): File {
        val file = File(context.cacheDir, "DeviceInspector_${ts()}.csv")
        val rows = mutableListOf("section,label,value")
        sections.forEach { s -> s.items.forEach { i -> rows.add("\"${s.title}\",\"${i.label}\",\"${i.value.replace("\"", "'")}\"") } }
        file.writeText(rows.joinToString("\n"))
        return file
    }

    fun exportJson(context: Context, sections: List<InfoSection>): File {
        val file = File(context.cacheDir, "DeviceInspector_${ts()}.json")
        val json = buildString {
            append("[")
            append(sections.joinToString(",") { s ->
                val items = s.items.joinToString(",") { i -> "{\"label\":\"${i.label}\",\"value\":\"${i.value.replace("\"", "'")}\"}" }
                "{\"title\":\"${s.title}\",\"items\":[$items]}"
            })
            append("]")
        }
        file.writeText(json)
        return file
    }

    private fun ts(): String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
}
