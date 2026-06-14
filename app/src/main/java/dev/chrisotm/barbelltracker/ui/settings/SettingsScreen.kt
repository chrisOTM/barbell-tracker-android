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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
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

private const val SYSTEM_TAG = "system"

private val LANGUAGES = listOf(
    LanguageOption("en", R.string.language_english),
    LanguageOption("de", R.string.language_german),
    LanguageOption(SYSTEM_TAG, R.string.language_system)
)

@Composable
fun SettingsScreen() {
    // Current app language: empty locale list = follow system, else the chosen tag.
    val locales = AppCompatDelegate.getApplicationLocales()
    val currentTag =
        if (locales.isEmpty) SYSTEM_TAG
        else locales.toLanguageTags().substringBefore('-').ifBlank { SYSTEM_TAG }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
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

        AboutSection()
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
