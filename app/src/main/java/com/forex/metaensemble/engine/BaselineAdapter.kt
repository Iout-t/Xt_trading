package com.forex.metaensemble.engine

import com.forex.metaensemble.model.BaselineProposal
import com.forex.metaensemble.model.MarketSnapshot

/**
 * Integration boundary for the frozen V1.2/H-Q baseline.
 *
 * This demo adapter does NOT claim to reproduce the original trained RF.
 * Replace only this implementation when the verified baseline artifact is available.
 */
interface BaselineAdapter {
    fun generateProposal(market: MarketSnapshot): BaselineProposal
}
