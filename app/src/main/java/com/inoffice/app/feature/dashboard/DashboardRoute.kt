package com.inoffice.app.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.inoffice.app.R
import com.inoffice.app.core.domain.DayType
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardRoute(
    onOpenReport: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var greetingName by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        greetingName = greetingDisplayName(GoogleSignIn.getLastSignedInAccount(context))
    }
    val monthTitle =
        remember(state.yearMonth) {
            state.yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
        }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onOpenReport) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.cd_month_report))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.cd_settings))
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            val firstName = greetingName
            Text(
                text =
                    if (firstName.isNullOrBlank()) {
                        stringResource(R.string.dashboard_greeting_fallback)
                    } else {
                        stringResource(R.string.dashboard_greeting, firstName)
                    },
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleMedium,
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.dashboard_office_count, state.officeDays, state.adjustedTarget),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text =
                            stringResource(
                                R.string.dashboard_breakdown,
                                state.baseMandate,
                                state.leaveDays,
                                state.holidayDays,
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val denom = max(state.adjustedTarget, 1)
                    val progress = (state.officeDays.toFloat() / denom).coerceIn(0f, 1f)
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                }
            }
            Text(text = stringResource(R.string.dashboard_today), style = MaterialTheme.typography.titleSmall)
            Text(
                text =
                    when (state.todayType) {
                        DayType.OFFICE -> stringResource(R.string.marked_office)
                        DayType.LEAVE -> stringResource(R.string.marked_leave)
                        DayType.HOLIDAY -> stringResource(R.string.marked_holiday)
                        DayType.NONE -> stringResource(R.string.marked_none)
                        null -> stringResource(R.string.marked_none)
                    },
                style = MaterialTheme.typography.bodyLarge,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.markToday(DayType.OFFICE) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_mark_office))
                }
                OutlinedButton(
                    onClick = { viewModel.markToday(DayType.LEAVE) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_mark_leave))
                }
                OutlinedButton(
                    onClick = { viewModel.markToday(DayType.HOLIDAY) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_mark_holiday))
                }
                OutlinedButton(
                    onClick = { viewModel.markToday(DayType.NONE) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_clear_today))
                }
            }
        }
    }
}

private fun greetingDisplayName(account: GoogleSignInAccount?): String? {
    if (account == null) {
        return null
    }
    val display = account.displayName?.trim().orEmpty()
    if (display.isNotEmpty()) {
        return display.split(Regex("\\s+")).firstOrNull { it.isNotBlank() }
    }
    val given = account.givenName?.trim().orEmpty()
    if (given.isNotEmpty()) {
        return given
    }
    val email = account.email?.trim().orEmpty()
    if (email.contains("@")) {
        val local = email.substringBefore("@").substringBefore(".")
        return local.takeUnless { it.isBlank() }
    }
    return null
}
