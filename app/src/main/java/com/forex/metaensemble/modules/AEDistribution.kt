package com.forex.metaensemble.modules

import com.forex.metaensemble.model.*

class AEDistribution {
    companion object { const val ID = "AE" }
    fun observe(market: MarketSnapshot, proposal: BaselineProposal): ModuleObservation {

        val returns = market.candles.zipWithNext().map { (a, b) -> (b.close / a.close) - 1.0 }
        if (returns.size < 10) return ModuleObservation(ID, ModuleState.SHADOW, 0.5, note = "Insufficient returns")
        val mean = returns.average()
        val variance = returns.map { (it - mean) * (it - mean) }.average()
        val sd = kotlin.math.sqrt(variance).coerceAtLeast(1e-9)
        val z = ((returns.last() - mean) / sd).coerceIn(-4.0, 4.0)
        val score = (0.5 + z / 8.0).coerceIn(0.0, 1.0)
        return ModuleObservation(ID, ModuleState.SHADOW, score, note = "Return z %.2f".format(z))

    }
}
