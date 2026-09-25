package com.forex.metaensemble.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetaEnsembleApp(viewModel: MetaEnsembleViewModel) {
    val quickPairs = listOf(
        "BTCUSD" to "BTC/USD",
        "EURUSD" to "EUR/USD",
        "USDJPY" to "USD/JPY",
        "GBPUSD" to "GBP/USD",
        "USDAUD" to "USD/AUD",
        "USDCAD" to "USD/CAD"
    )
    val decision by viewModel.decision.collectAsState()
    val running by viewModel.running.collectAsState()
    val instruments by viewModel.instruments.collectAsState()
    val selectedSymbol by viewModel.selectedSymbol.collectAsState()
    val proxyUrl by viewModel.proxyUrl.collectAsState()
    val status by viewModel.status.collectAsState()
    val error by viewModel.error.collectAsState()
    var symbolDraft by remember(selectedSymbol) { mutableStateOf(selectedSymbol) }
    var proxyDraft by remember(proxyUrl) { mutableStateOf(proxyUrl) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Xt Trading • Live Signals") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Real-time market signal generator", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Forex and crypto data is fetched through the secure Twelve Data proxy. Signals are analytical only; this app never places orders.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            item {
                Text(status, style = MaterialTheme.typography.bodySmall)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }

            item {
                OutlinedTextField(
                    value = proxyDraft,
                    onValueChange = { proxyDraft = it },
                    label = { Text("Signal proxy URL") },
                    supportingText = { Text("Emulator: 10.0.2.2:8787 • Phone: use your computer LAN IP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(onClick = { viewModel.setProxyUrl(proxyDraft); viewModel.refreshInstruments() }) {
                    Text("Apply proxy URL")
                }
            }

            item {
                OutlinedTextField(
                    value = symbolDraft,
                    onValueChange = { symbolDraft = it.uppercase() },
                    label = { Text("Symbol (for example USD/AUD or BTC/USD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text("Quick currency selection", style = MaterialTheme.typography.titleMedium)
                quickPairs.forEach { (label, symbol) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.setSymbol(symbol)
                            symbolDraft = symbol
                        },
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSymbol == symbol,
                            onClick = {
                                viewModel.setSymbol(symbol)
                                symbolDraft = symbol
                            }
                        )
                        Text("$label  ($symbol)")
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.setSymbol(symbolDraft); viewModel.evaluateOnce() }) {
                        Text("Get live signal")
                    }
                    OutlinedButton(onClick = { viewModel.toggleRunning() }) {
                        Text(if (running) "Stop updates" else "Start updates")
                    }
                    OutlinedButton(onClick = { viewModel.refreshInstruments() }) {
                        Text("Refresh symbols")
                    }
                }
            }

            decision?.let { d ->
                item {
                    Card {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${d.pair}: ${d.signal}", style = MaterialTheme.typography.headlineSmall)
                            Text("Confidence: ${(d.confidence * 100).toInt()}%")
                            Text("Reference price: ${"%.6f".format(d.referencePrice)}")
                            Text("Baseline: ${d.baseline.signal} @ ${(d.baseline.probability * 100).toInt()}%")
                            Text("Size multiplier: %.2f".format(d.sizeMultiplier))
                            Text(if (d.vetoed) "Decision: VETO / WAIT" else "Decision: analytical signal")
                        }
                    }
                }

                item { Text("Module observations", style = MaterialTheme.typography.titleMedium) }
                items(d.observations) { observation ->
                    ListItem(
                        headlineContent = { Text("${observation.id} • ${observation.state}") },
                        supportingContent = {
                            Text("score=%.3f  size=%.2f  %s".format(observation.score, observation.sizeMultiplier, observation.note))
                        },
                        trailingContent = {
                            if (observation.veto) Text("VETO", color = MaterialTheme.colorScheme.error)
                        }
                    )
                    HorizontalDivider()
                }
            }

            item {
                Text("Provider-supported symbols (${instruments.size})", style = MaterialTheme.typography.titleMedium)
                Text("Tap a symbol to select it. The list includes all instruments returned by the provider.", style = MaterialTheme.typography.bodySmall)
            }

            items(instruments, key = { it.symbol }) { instrument ->
                ListItem(
                    modifier = Modifier.clickable {
                        viewModel.setSymbol(instrument.symbol)
                        symbolDraft = instrument.symbol
                    },
                    headlineContent = { Text(instrument.symbol) },
                    supportingContent = { Text("${instrument.assetClass.uppercase()} • ${instrument.displayName}") }
                )
                HorizontalDivider()
            }

            item {
                Text(
                    "Risk notice: signals can be delayed, incomplete, or wrong. Validate pricing, spread, liquidity, timeframe, and risk independently before making any decision. No live order execution is included.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
