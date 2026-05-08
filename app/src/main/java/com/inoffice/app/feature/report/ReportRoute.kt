package com.inoffice.app.feature.report

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inoffice.app.R
import com.inoffice.app.core.domain.DayEntry
import com.inoffice.app.core.domain.DayType
import com.inoffice.app.ui.MarkDayTypeDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportRoute(
    onBack: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val monthTitle =
        remember(state.yearMonth) {
            state.yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
        }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()) }
    var dialogTargetDate by remember { mutableStateOf<LocalDate?>(null) }
    val pendingDate = dialogTargetDate
    if (pendingDate != null) {
        val formatted = pendingDate.format(dateFormatter)
        MarkDayTypeDialog(
            title = stringResource(R.string.report_edit_mark_title, formatted),
            body = stringResource(R.string.report_edit_mark_body),
            showClearOption = true,
            onDismiss = { dialogTargetDate = null },
            onSelectType = { type ->
                viewModel.setDayTypeForDate(pendingDate, type)
                dialogTargetDate = null
            },
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_title)) },
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
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = viewModel::goToPreviousMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.report_prev_month))
                }
                Text(text = monthTitle, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = viewModel::goToNextMonth) {
                    Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.report_next_month))
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text =
                        stringResource(
                            R.string.report_summary,
                            state.summary.mandateProgressDays,
                            state.summary.adjustedTarget,
                            state.summary.baseMandate,
                        ),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            val hasAnyContent = state.entries.isNotEmpty() || state.unmarkedWeekdays.isNotEmpty()
            if (!hasAnyContent) {
                Text(
                    text = stringResource(R.string.report_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.unmarkedWeekdays.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.report_unmarked_section),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                            )
                        }
                        items(state.unmarkedWeekdays, key = { "unmarked-$it" }) { date ->
                            UnmarkedRow(
                                date = date,
                                dateFormatter = dateFormatter,
                                onClick = { dialogTargetDate = date },
                            )
                        }
                    }
                    if (state.entries.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.report_marked_section),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                            )
                        }
                        items(state.entries, key = { it.localDate }) { entry ->
                            MarkedReportRow(
                                entry = entry,
                                dateFormatter = dateFormatter,
                                onClick = { dialogTargetDate = entry.localDate },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnmarkedRow(
    date: LocalDate,
    dateFormatter: DateTimeFormatter,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ),
    ) {
        Text(
            text =
                stringResource(
                    R.string.report_unmarked_row,
                    date.format(dateFormatter),
                ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun MarkedReportRow(
    entry: DayEntry,
    dateFormatter: DateTimeFormatter,
    onClick: () -> Unit,
) {
    val rowColors =
        when (entry.type) {
            DayType.OFFICE ->
                MaterialTheme.colorScheme.primaryContainer to
                    MaterialTheme.colorScheme.onPrimaryContainer
            DayType.LEAVE ->
                MaterialTheme.colorScheme.tertiaryContainer to
                    MaterialTheme.colorScheme.onTertiaryContainer
            DayType.HOLIDAY ->
                MaterialTheme.colorScheme.errorContainer to
                    MaterialTheme.colorScheme.onErrorContainer
            DayType.WFH ->
                MaterialTheme.colorScheme.secondaryContainer to
                    MaterialTheme.colorScheme.onSecondaryContainer
            DayType.NONE ->
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) to
                    MaterialTheme.colorScheme.onSurfaceVariant
        }
    val typeLabel =
        stringResource(
            when (entry.type) {
                DayType.OFFICE -> R.string.type_office
                DayType.LEAVE -> R.string.type_leave
                DayType.HOLIDAY -> R.string.type_holiday
                DayType.WFH -> R.string.type_wfh
                DayType.NONE -> R.string.marked_none
            },
        )
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        colors =
            CardDefaults.cardColors(
                containerColor = rowColors.first,
            ),
    ) {
        Text(
            text = stringResource(R.string.report_row, entry.localDate.format(dateFormatter), typeLabel),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = rowColors.second,
        )
    }
}
