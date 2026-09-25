package com.forex.metaensemble.modules

import com.forex.metaensemble.model.*

class AIAnnouncement {
    companion object { const val ID = "AI" }
    fun observe(market: MarketSnapshot, proposal: BaselineProposal): ModuleObservation {

        // No external news provider is wired in. Keep this shadow and neutral until
        // timestamped, point-in-time announcement data is available.
        return ModuleObservation(ID, ModuleState.SHADOW, 0.5, note = "No announcement feed")

    }
}
