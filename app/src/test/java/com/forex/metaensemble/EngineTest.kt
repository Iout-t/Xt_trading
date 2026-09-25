package com.forex.metaensemble

import com.forex.metaensemble.data.SimulatedMarket
import com.forex.metaensemble.engine.MetaEnsembleEngine
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineTest {
    @Test
    fun engineProducesDecision() {
        val market = SimulatedMarket().next()
        val decision = MetaEnsembleEngine().evaluate(market)
        assertTrue(decision.confidence in 0.0..1.0)
        assertTrue(decision.sizeMultiplier in 0.0..1.0)
    }
}
