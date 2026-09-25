package com.forex.metaensemble.modules

import com.forex.metaensemble.model.*

class AHCapacity {
    companion object { const val ID = "AH" }
    fun observe(market: MarketSnapshot, proposal: BaselineProposal): ModuleObservation {

        val spreadPenalty = (market.spread / 0.0002).coerceAtMost(4.0)
        val size = (1.0 - 0.15 * spreadPenalty).coerceIn(0.25, 1.0)
        return ModuleObservation(ID, ModuleState.SHADOW, size, sizeMultiplier = size,
            note = "Spread-based capacity proxy")

    }
}
