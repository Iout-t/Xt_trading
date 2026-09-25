package com.forex.metaensemble.engine

import com.forex.metaensemble.modules.*
import com.forex.metaensemble.model.*

class MetaEnsembleEngine(
    private val baseline: BaselineAdapter = DemoBaselineAdapter(),
    private val decisionEngine: DecisionEngine = DecisionEngine()
) {
    private val modules: List<(MarketSnapshot, BaselineProposal) -> ModuleObservation> = listOf(
        { market, proposal -> ABEventTime().observe(market, proposal) },
        { market, proposal -> ACTriangular().observe(market, proposal) },
        { market, proposal -> ADOrderFlow().observe(market, proposal) },
        { market, proposal -> AEDistribution().observe(market, proposal) },
        { market, proposal -> AFExpertAggregator().observe(market, proposal) },
        { market, proposal -> AGVetoAudit().observe(market, proposal) },
        { market, proposal -> AHCapacity().observe(market, proposal) },
        { market, proposal -> AIAnnouncement().observe(market, proposal) }
    )

    fun evaluate(market: MarketSnapshot): EngineDecision {
        val proposal = baseline.generateProposal(market)
        val observations = modules.map { observer -> observer(market, proposal) }
        return decisionEngine.compose(market.pair, proposal, observations)
    }
}
