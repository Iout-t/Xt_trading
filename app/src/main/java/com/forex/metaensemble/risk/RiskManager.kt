package com.forex.metaensemble.risk

import com.forex.metaensemble.config.StrategyConfig
import com.forex.metaensemble.model.EngineDecision

class RiskManager(private val config: StrategyConfig = StrategyConfig()) {
    fun allowedRiskFraction(decision: EngineDecision, dailyDrawdown: Double): Double {
        if (dailyDrawdown >= config.dailyDrawdownLimit) return 0.0
        if (decision.vetoed) return 0.0
        return (config.maxRiskPerTrade * decision.sizeMultiplier)
            .coerceIn(0.0, config.maxRiskPerTrade)
    }
}
