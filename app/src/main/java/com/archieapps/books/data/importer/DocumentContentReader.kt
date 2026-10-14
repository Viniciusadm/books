package com.archieapps.books.data.importer

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.archieapps.books.domain.ImportRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DocumentContentReader(private val contentResolver: ContentResolver) {

    suspend fun read(uris: List<Uri>): List<ImportRequest> = withContext(Dispatchers.IO) {
        uris.map { uri ->
            ImportRequest(
                fileName = displayName(uri) ?: uri.lastPathSegment.orEmpty(),
                content = runCatching {
                    contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
                }.getOrNull(),
            )
        }
    }

    private fun displayName(uri: Uri): String? = runCatching {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
    }.getOrNull()
}
