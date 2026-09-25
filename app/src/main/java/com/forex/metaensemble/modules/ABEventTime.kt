package com.forex.metaensemble.modules

import com.forex.metaensemble.model.*

class ABEventTime {
    companion object { const val ID = "AB" }
    fun observe(market: MarketSnapshot, proposal: BaselineProposal): ModuleObservation {

        val candles = market.candles.takeLast(20)
        val ranges = candles.map { it.high - it.low }.filter { it > 0 }
        if (ranges.isEmpty()) return ModuleObservation(ID, ModuleState.SHADOW, 0.5, note = "No range data")
        val avg = ranges.average()
        val last = ranges.last()
        val score = (last / (avg + 1e-9)).coerceIn(0.0, 2.0) / 2.0
        return ModuleObservation(ID, ModuleState.SHADOW, score, note = "Activity ratio %.2f".format(last / avg))

    }
}
