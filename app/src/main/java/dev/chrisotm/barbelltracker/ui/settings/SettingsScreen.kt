package dev.chrisotm.barbelltracker.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisotm.barbelltracker.BuildConfig
import dev.chrisotm.barbelltracker.R
import dev.chrisotm.barbelltracker.data.repo.ImportMode
import java.time.LocalDate

private data class LanguageOption(val tag: String, val labelRes: Int)

private const val SYSTEM_TAG = "system"

private val LANGUAGES = listOf(
    LanguageOption("en", R.string.language_english),
    LanguageOption("de", R.string.language_german),
    LanguageOption(SYSTEM_TAG, R.string.language_system)
)

/** Which dataset an in-flight import targets. */
private enum class ImportTarget { WORKOUTS, HISTORY }

@Composable
fun SettingsScreen() {
    // Current app language: empty locale list = follow system, else the chosen tag.
    val locales = AppCompatDelegate.getApplicationLocales()
    val currentTag =
        if (locales.isEmpty) SYSTEM_TAG
        else locales.toLanguageTags().substringBefore('-').ifBlank { SYSTEM_TAG }

    val snackbarHostState = remember { SnackbarHostState() }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
            LANGUAGES.forEach { lang ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = currentTag == lang.tag,
                            onClick = {
                                if (currentTag != lang.tag) {
                                    AppCompatDelegate.setApplicationLocales(
                                        if (lang.tag == SYSTEM_TAG) LocaleListCompat.getEmptyLocaleList()
                                        else LocaleListCompat.forLanguageTags(lang.tag)
                                    )
                                }
                            }
                        )
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = currentTag == lang.tag, onClick = null)
                    Text(
                        stringResource(lang.labelRes),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 20.dp))

            DataSection(snackbarHostState)

            HorizontalDivider(Modifier.padding(vertical = 20.dp))

            AboutSection()
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun DataSection(snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val vm: ImportExportViewModel = hiltViewModel()
    val message by vm.message.collectAsStateWithLifecycle()

    // Pending import state: which dataset, and (after dialog) the chosen mode.
    var pendingTarget by remember { mutableStateOf<ImportTarget?>(null) }
    var modeDialogFor by remember { mutableStateOf<ImportTarget?>(null) }

    val exportWorkoutsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { vm.exportWorkouts(it) } }

    val exportHistoryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { vm.exportHistory(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        val target = pendingTarget
        val mode = pendingMode
        pendingTarget = null
        if (uri != null && target != null && mode != null) {
            when (target) {
                ImportTarget.WORKOUTS -> vm.importWorkouts(uri, mode)
                ImportTarget.HISTORY -> vm.importHistory(uri, mode)
            }
        }
    }

    // Resolve snackbar text + show.
    val parseError = stringResource(R.string.import_error_parse)
    val wrongType = stringResource(R.string.import_error_wrong_type)
    val exportDone = stringResource(R.string.export_done)
    LaunchedEffect(message) {
        val msg = message ?: return@LaunchedEffect
        val text = when (msg) {
            BackupMessage.Exported -> exportDone
            is BackupMessage.Imported ->
                context.getString(R.string.import_done, msg.result.imported, msg.result.skipped)
            BackupMessage.ErrorParse -> parseError
            BackupMessage.ErrorWrongType -> wrongType
        }
        snackbarHostState.showSnackbar(text)
        vm.consumeMessage()
    }

    Text(stringResource(R.string.data_section), style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))

    ClickableRow(stringResource(R.string.export_workouts)) {
        exportWorkoutsLauncher.launch(suggestedName("workouts"))
    }
    ClickableRow(stringResource(R.string.import_workouts)) {
        modeDialogFor = ImportTarget.WORKOUTS
    }
    ClickableRow(stringResource(R.string.backup_history)) {
        exportHistoryLauncher.launch(suggestedName("history"))
    }
    ClickableRow(stringResource(R.string.restore_history)) {
        modeDialogFor = ImportTarget.HISTORY
    }

    modeDialogFor?.let { target ->
        ImportModeDialog(
            onDismiss = { modeDialogFor = null },
            onPick = { mode ->
                modeDialogFor = null
                pendingTarget = target
                pendingMode = mode
                importLauncher.launch(arrayOf("application/json"))
            }
        )
    }
}

// Mode chosen in the dialog, read back inside the launcher callback.
private var pendingMode: ImportMode? = null

@Composable
private fun ImportModeDialog(onDismiss: () -> Unit, onPick: (ImportMode) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.import_mode_title)) },
        text = { Text(stringResource(R.string.import_replace_warning)) },
        confirmButton = {
            TextButton(onClick = { onPick(ImportMode.MERGE) }) {
                Text(stringResource(R.string.import_merge))
            }
        },
        dismissButton = {
            TextButton(onClick = { onPick(ImportMode.REPLACE) }) {
                Text(stringResource(R.string.import_replace))
            }
        }
    )
}

@Composable
private fun ClickableRow(label: String, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    )
}

private fun suggestedName(kind: String): String =
    "barbell-$kind-${LocalDate.now()}.json"

private const val REPO_URL = "https://github.com/chrisOTM/barbell-tracker-android"

@Composable
private fun AboutSection() {
    val uriHandler = LocalUriHandler.current
    Text(stringResource(R.string.about), style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    Text(
        stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        stringResource(R.string.about_developer, "chrisOTM"),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(top = 4.dp)
    )
    Text(
        stringResource(R.string.about_repository),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(top = 8.dp)
            .clickable { uriHandler.openUri(REPO_URL) }
    )
}
