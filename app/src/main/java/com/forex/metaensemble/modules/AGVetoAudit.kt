package com.forex.metaensemble.modules

import com.forex.metaensemble.model.*

class AGVetoAudit {
    companion object { const val ID = "AG" }
    fun observe(market: MarketSnapshot, proposal: BaselineProposal): ModuleObservation {

        // Governance-only: never originates a trade and never overrides the frozen baseline here.
        return ModuleObservation(ID, ModuleState.SHADOW, 0.5, note = "Audit observation only")

    }
}
