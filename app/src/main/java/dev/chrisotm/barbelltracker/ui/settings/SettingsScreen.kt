package dev.chrisotm.barbelltracker.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import dev.chrisotm.barbelltracker.BuildConfig
import dev.chrisotm.barbelltracker.R

private data class LanguageOption(val tag: String, val labelRes: Int)

private val LANGUAGES = listOf(
    LanguageOption("en", R.string.language_english),
    LanguageOption("de", R.string.language_german)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    // Current app language tag (e.g. "en" / "de"); falls back to English.
    val currentTag = AppCompatDelegate.getApplicationLocales()
        .toLanguageTags().substringBefore('-').ifBlank { "en" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
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
                                        LocaleListCompat.forLanguageTags(lang.tag)
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

            AboutSection()
        }
    }
}

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
        stringResource(R.string.about_developer, "ChrisOTM"),
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
