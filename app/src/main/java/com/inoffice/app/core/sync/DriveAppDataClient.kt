package com.inoffice.app.core.sync

import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

@Singleton
class DriveAppDataClient @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun downloadSnapshot(): String? {
        val fileId = getOrCreateSnapshotFileId() ?: return null
        val token = getAccessToken()
        val request = authorizedConnection("GET", "https://www.googleapis.com/drive/v3/files/$fileId?alt=media", token)
        return request.inputStream.use { stream ->
            BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).readText()
        }
    }

    suspend fun uploadSnapshot(json: String) {
        val fileId = getOrCreateSnapshotFileId()
        val token = getAccessToken()
        if (fileId == null) {
            createSnapshotFile(json, token)
        } else {
            updateSnapshotFile(fileId, json, token)
        }
    }

    private fun getOrCreateSnapshotFileId(): String? {
        val token = getAccessToken()
        val query = URLEncoder.encode("name = '$SYNC_FILE_NAME' and trashed = false", StandardCharsets.UTF_8.name())
        val url = "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=$query&fields=files(id,name)"
        val connection = authorizedConnection("GET", url, token)
        val body =
            connection.inputStream.use { stream ->
                BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).readText()
            }
        val files = JSONObject(body).optJSONArray("files")
        if (files != null && files.length() > 0) {
            return files.getJSONObject(0).getString("id")
        }
        return null
    }

    private fun createSnapshotFile(json: String, token: String) {
        val boundary = "inoffice-${UUID.randomUUID()}"
        val metadata =
            JSONObject()
                .put("name", SYNC_FILE_NAME)
                .put("parents", org.json.JSONArray().put("appDataFolder"))
                .put("mimeType", "application/json")
                .toString()
        val body =
            buildString {
                append("--$boundary\r\n")
                append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
                append(metadata)
                append("\r\n")
                append("--$boundary\r\n")
                append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
                append(json)
                append("\r\n--$boundary--\r\n")
            }
        val connection =
            authorizedConnection(
                "POST",
                "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart",
                token,
            ).apply {
                setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
                doOutput = true
            }
        connection.outputStream.use { output ->
            output.write(body.toByteArray(StandardCharsets.UTF_8))
        }
        ensureSuccess(connection)
    }

    private fun updateSnapshotFile(fileId: String, json: String, token: String) {
        val connection =
            authorizedConnection(
                "PATCH",
                "https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media",
                token,
            ).apply {
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
            }
        connection.outputStream.use { output ->
            output.write(json.toByteArray(StandardCharsets.UTF_8))
        }
        ensureSuccess(connection)
    }

    private fun getAccessToken(): String {
        val account =
            GoogleSignIn.getLastSignedInAccount(context)?.account
                ?: throw IllegalStateException("Google account not available for Drive sync")
        return try {
            GoogleAuthUtil.getToken(context, account, DRIVE_SCOPE)
        } catch (error: UserRecoverableAuthException) {
            throw IllegalStateException("Drive access needs user consent", error)
        }
    }

    private fun authorizedConnection(
        method: String,
        url: String,
        token: String,
    ): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 15_000
            readTimeout = 20_000
        }

    private fun ensureSuccess(connection: HttpURLConnection) {
        val code = connection.responseCode
        if (code in 200..299) {
            return
        }
        val errorBody =
            connection.errorStream?.use { stream ->
                BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).readText()
            }.orEmpty()
        throw IllegalStateException("Drive request failed: $code $errorBody")
    }

    companion object {
        private const val SYNC_FILE_NAME = "inoffice_sync.json"
        private const val DRIVE_SCOPE = "oauth2:https://www.googleapis.com/auth/drive.appdata"
    }
}
