package com.forex.metaensemble.modules

import com.forex.metaensemble.model.*

class ACTriangular {
    companion object { const val ID = "AC" }
    fun observe(market: MarketSnapshot, proposal: BaselineProposal): ModuleObservation {

        val price = market.lastPrice
        if (price <= 0.0) return ModuleObservation(ID, ModuleState.SHADOW, 0.5, note = "No price")
        // Placeholder until verified multi-feed EUR/USD, GBP/USD and USD/JPY inputs are connected.
        return ModuleObservation(ID, ModuleState.SHADOW, 0.5, note = "Awaiting three-feed residual")

    }
}
