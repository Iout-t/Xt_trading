package com.forex.metaensemble.ui

import androidx.lifecycle.ViewModel
import com.forex.metaensemble.data.SimulatedMarket
import com.forex.metaensemble.engine.MetaEnsembleEngine
import com.forex.metaensemble.model.EngineDecision
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MetaEnsembleViewModel : ViewModel() {
    private val market = SimulatedMarket()
    private val engine = MetaEnsembleEngine()

    private val _decision = MutableStateFlow<EngineDecision?>(null)
    val decision: StateFlow<EngineDecision?> = _decision

    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running

    fun evaluateOnce() {
        _decision.value = engine.evaluate(market.next())
    }

    fun toggleRunning() {
        _running.value = !_running.value
        if (_running.value) evaluateOnce()
    }
}
