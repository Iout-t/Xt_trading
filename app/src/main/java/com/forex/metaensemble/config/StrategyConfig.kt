package com.forex.metaensemble.config

data class StrategyConfig(
    val version: String = "2.3.0",
    val universe: List<String> = listOf("EUR/USD", "GBP/USD", "USD/JPY"),
    val macroTimeframe: String = "H1",
    val structureTimeframe: String = "M15",
    val executionTimeframe: String = "M5",
    val probabilityThreshold: Double = 0.55,
    val maxRiskPerTrade: Double = 0.015,
    val maxLeverage: Double = 10.0,
    val dailyDrawdownLimit: Double = 0.025,
    val spreadMultiplier: Double = 3.0,
    val turnoverTarget: Double = 0.004
)
