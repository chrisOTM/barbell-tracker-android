package dev.chrisotm.barbelltracker.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.chrisotm.barbelltracker.data.io.HistoryBackup
import dev.chrisotm.barbelltracker.data.io.TYPE_HISTORY
import dev.chrisotm.barbelltracker.data.io.TYPE_WORKOUTS
import dev.chrisotm.barbelltracker.data.io.WorkoutsBackup
import dev.chrisotm.barbelltracker.data.repo.BackupRepository
import dev.chrisotm.barbelltracker.data.repo.ImportMode
import dev.chrisotm.barbelltracker.data.repo.ImportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/** Outcome of an import/export op, surfaced to the UI for a snackbar. */
sealed interface BackupMessage {
    data object Exported : BackupMessage
    data class Imported(val result: ImportResult) : BackupMessage
    data object ErrorParse : BackupMessage
    data object ErrorWrongType : BackupMessage
}

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: BackupRepository
) : ViewModel() {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    private val _message = MutableStateFlow<BackupMessage?>(null)
    val message: StateFlow<BackupMessage?> = _message.asStateFlow()

    fun consumeMessage() { _message.value = null }

    fun exportWorkouts(uri: Uri) = export(uri) { json.encodeToString(repo.buildWorkoutsBackup()) }

    fun exportHistory(uri: Uri) = export(uri) { json.encodeToString(repo.buildHistoryBackup()) }

    fun importWorkouts(uri: Uri, mode: ImportMode) = viewModelScope.launch {
        runImport(uri) { text ->
            val backup = json.decodeFromString<WorkoutsBackup>(text)
            if (backup.type != TYPE_WORKOUTS) return@runImport null
            repo.importWorkouts(backup, mode)
        }
    }

    fun importHistory(uri: Uri, mode: ImportMode) = viewModelScope.launch {
        runImport(uri) { text ->
            val backup = json.decodeFromString<HistoryBackup>(text)
            if (backup.type != TYPE_HISTORY) return@runImport null
            repo.importHistory(backup, mode)
        }
    }

    private fun export(uri: Uri, produce: suspend () -> String) = viewModelScope.launch {
        _message.value = try {
            withContext(Dispatchers.IO) {
                val text = produce()
                context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
                    ?: error("null stream")
            }
            BackupMessage.Exported
        } catch (e: Exception) {
            BackupMessage.ErrorParse
        }
    }

    /** [decode] returns null when the file is well-formed JSON but the wrong backup type. */
    private suspend fun runImport(uri: Uri, decode: suspend (String) -> ImportResult?) {
        _message.value = try {
            val result = withContext(Dispatchers.IO) {
                val text = context.contentResolver.openInputStream(uri)?.use {
                    it.readBytes().decodeToString()
                } ?: error("null stream")
                decode(text)
            }
            if (result == null) BackupMessage.ErrorWrongType else BackupMessage.Imported(result)
        } catch (e: Exception) {
            BackupMessage.ErrorParse
        }
    }
}
