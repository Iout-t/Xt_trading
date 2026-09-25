package com.forex.metaensemble.modules

import com.forex.metaensemble.model.*

class ADOrderFlow {
    companion object { const val ID = "AD" }
    fun observe(market: MarketSnapshot, proposal: BaselineProposal): ModuleObservation {

        val candles = market.candles.takeLast(12)
        if (candles.isEmpty()) return ModuleObservation(ID, ModuleState.SHADOW, 0.5)
        val signed = candles.sumOf { if (it.close >= it.open) it.volume else -it.volume }
        val total = candles.sumOf { it.volume }.coerceAtLeast(1e-9)
        val score = ((signed / total) + 1.0) / 2.0
        return ModuleObservation(ID, ModuleState.SHADOW, score.coerceIn(0.0, 1.0), note = "Candle-volume proxy")

    }
}
