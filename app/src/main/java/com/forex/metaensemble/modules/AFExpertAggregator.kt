package com.forex.metaensemble.modules

import com.forex.metaensemble.model.*

class AFExpertAggregator {
    companion object { const val ID = "AF" }
    fun observe(market: MarketSnapshot, proposal: BaselineProposal): ModuleObservation {

        val scores = listOf(
            proposal.probability,
            market.candles.takeLast(20).let { cs ->
                if (cs.size < 2) 0.5 else {
                    val up = cs.zipWithNext().count { it.second.close > it.first.close }
                    up.toDouble() / (cs.size - 1)
                }
            }
        )
        val mean = scores.average()
        val disagreement = scores.maxOrNull()!! - scores.minOrNull()!!
        return ModuleObservation(ID, ModuleState.SHADOW, mean, note = "Disagreement %.3f".format(disagreement))

    }
}
