package com.forex.metaensemble.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.forex.metaensemble.model.Signal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetaEnsembleApp(viewModel: MetaEnsembleViewModel) {
    val decision by viewModel.decision.collectAsState()
    val running by viewModel.running.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("MetaEnsemble FX • V2.3") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Research / Paper Mode",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "The frozen baseline is represented by an explicit adapter. " +
                        "AB–AI remain shadow-only until their data and acceptance gates are verified.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.evaluateOnce() }) {
                        Text("Generate signal")
                    }
                    OutlinedButton(onClick = { viewModel.toggleRunning() }) {
                        Text(if (running) "Running" else "Start")
                    }
                }
            }

            decision?.let { d ->
                item {
                    Card {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Signal: ${d.signal}", style = MaterialTheme.typography.headlineSmall)
                            Text("Confidence: ${(d.confidence * 100).toInt()}%")
                            Text("Baseline: ${d.baseline.signal} @ ${(d.baseline.probability * 100).toInt()}%")
                            Text("Size multiplier: %.2f".format(d.sizeMultiplier))
                            Text(if (d.vetoed) "Decision: VETO / WAIT" else "Decision: baseline proposal retained")
                        }
                    }
                }

                item {
                    Text("Module observations", style = MaterialTheme.typography.titleMedium)
                }

                items(d.observations) { o ->
                    ListItem(
                        headlineContent = { Text("${o.id} • ${o.state}") },
                        supportingContent = {
                            Text("score=%.3f  size=%.2f  %s".format(o.score, o.sizeMultiplier, o.note))
                        },
                        trailingContent = {
                            if (o.veto) Text("VETO", color = MaterialTheme.colorScheme.error)
                        }
                    )
                    HorizontalDivider()
                }
            }

            item {
                Text(
                    "Safety: this build does not place live orders and does not claim to reproduce the frozen RF baseline.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
