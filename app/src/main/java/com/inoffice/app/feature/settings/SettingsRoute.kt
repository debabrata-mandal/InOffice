package com.inoffice.app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inoffice.app.R
import com.inoffice.app.core.sync.SyncState
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    signedInEmail: String?,
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val mandate by viewModel.baseMandate.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf(mandate.toString()) }
    LaunchedEffect(mandate) {
        draft = mandate.toString()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.report_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it.filter { ch -> ch.isDigit() } },
                label = { Text(stringResource(R.string.settings_mandate_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    val parsed = draft.toIntOrNull() ?: mandate
                    viewModel.saveBaseMandate(parsed)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_save))
            }
            Button(
                onClick = { viewModel.syncNow() },
                enabled = syncStatus.state != SyncState.SYNCING,
                colors = ButtonDefaults.buttonColors(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    when (syncStatus.state) {
                        SyncState.QUEUED -> stringResource(R.string.settings_sync_queued)
                        SyncState.SYNCING -> stringResource(R.string.settings_sync_in_progress)
                        else -> stringResource(R.string.settings_sync_now)
                    },
                )
            }
            Text(
                text =
                    when (syncStatus.state) {
                        SyncState.QUEUED -> stringResource(R.string.settings_sync_status_queued)
                        SyncState.SYNCING -> stringResource(R.string.settings_sync_status_syncing)
                        SyncState.ERROR -> syncStatus.errorMessage ?: stringResource(R.string.settings_sync_status_error)
                        SyncState.IDLE -> {
                            val at = syncStatus.lastSuccessAtEpochMillis
                            if (at != null) {
                                val formatted = DateFormat.getDateTimeInstance().format(Date(at))
                                stringResource(R.string.settings_sync_status_success, formatted)
                            } else {
                                stringResource(R.string.settings_sync_status_idle)
                            }
                        }
                    },
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (syncStatus.state == SyncState.ERROR) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text =
                    if (signedInEmail.isNullOrBlank()) {
                        stringResource(R.string.settings_account_unknown)
                    } else {
                        stringResource(R.string.settings_signed_in_as, signedInEmail)
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_sign_out))
            }
        }
    }
}
