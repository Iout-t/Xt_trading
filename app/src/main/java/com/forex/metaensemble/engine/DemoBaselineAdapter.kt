package com.forex.metaensemble.engine

import com.forex.metaensemble.model.BaselineProposal
import com.forex.metaensemble.model.MarketSnapshot
import com.forex.metaensemble.model.Signal
import kotlin.math.abs
import kotlin.math.max

class DemoBaselineAdapter : BaselineAdapter {
    override fun generateProposal(market: MarketSnapshot): BaselineProposal {
        val candles = market.candles
        if (candles.size < 30) return BaselineProposal(Signal.WAIT, 0.50, "DEMO_BASELINE")

        val closes = candles.takeLast(30).map { it.close }
        val mean = closes.average()
        val last = closes.last()
        val recent = closes.takeLast(8).average()
        val older = closes.take(8).average()
        val volatility = max(1e-8, closes.map { abs(it - mean) }.average())
        val z = (last - mean) / volatility

        val signal = when {
            z > 1.35 && recent < older -> Signal.PUT
            z < -1.35 && recent > older -> Signal.CALL
            abs(z) < 0.35 -> Signal.RANGE
            else -> Signal.WAIT
        }
        val confidence = (0.50 + minOf(0.20, abs(z) * 0.08)).coerceIn(0.50, 0.70)
        return BaselineProposal(signal, confidence, "DEMO_BASELINE")
    }
}
