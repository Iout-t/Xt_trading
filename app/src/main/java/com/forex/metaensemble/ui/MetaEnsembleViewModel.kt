package com.forex.metaensemble.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forex.metaensemble.BuildConfig
import com.forex.metaensemble.data.LiveMarketClient
import com.forex.metaensemble.engine.MetaEnsembleEngine
import com.forex.metaensemble.model.EngineDecision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MetaEnsembleViewModel : ViewModel() {
    private val client = LiveMarketClient(BuildConfig.SIGNAL_PROXY_URL)
    private val engine = MetaEnsembleEngine()
    private var loopJob: Job? = null

    private val _decision = MutableStateFlow<EngineDecision?>(null)
    val decision: StateFlow<EngineDecision?> = _decision

    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running

    private val _instruments = MutableStateFlow<List<LiveMarketClient.Instrument>>(emptyList())
    val instruments: StateFlow<List<LiveMarketClient.Instrument>> = _instruments

    private val _selectedSymbol = MutableStateFlow("EUR/USD")
    val selectedSymbol: StateFlow<String> = _selectedSymbol

    private val _status = MutableStateFlow("Connecting to live market data…")
    val status: StateFlow<String> = _status

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        refreshInstruments()
    }

    fun refreshInstruments() {
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.instruments() } }
                .onSuccess { available ->
                    _instruments.value = available
                    if (available.none { it.symbol == _selectedSymbol.value } && available.isNotEmpty()) {
                        _selectedSymbol.value = available.first().symbol
                    }
                    _status.value = "${available.size} provider instruments available"
                    _error.value = null
                }
                .onFailure { _error.value = it.message ?: "Unable to load instruments"; _status.value = "Proxy unavailable" }
        }
    }

    fun setSymbol(symbol: String) {
        val normalized = symbol.trim().uppercase()
        if (normalized.isNotBlank()) _selectedSymbol.value = normalized
    }

    fun evaluateOnce() {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { engine.evaluate(client.market(_selectedSymbol.value)) }
            }.onSuccess {
                _decision.value = it
                _status.value = "Live ${it.pair} • updated just now"
                _error.value = null
            }.onFailure {
                _error.value = it.message ?: "Unable to load market data"
                _status.value = "Live update failed"
            }
        }
    }

    fun toggleRunning() {
        if (_running.value) {
            loopJob?.cancel()
            loopJob = null
            _running.value = false
            _status.value = "Live updates paused"
        } else {
            _running.value = true
            loopJob = viewModelScope.launch {
                while (_running.value) {
                    evaluateOnce()
                    delay(30_000)
                }
            }
        }
    }

    override fun onCleared() {
        loopJob?.cancel()
        super.onCleared()
    }
}
