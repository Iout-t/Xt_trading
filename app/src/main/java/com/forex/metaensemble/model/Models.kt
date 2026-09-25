package com.forex.metaensemble.model

enum class Signal { CALL, PUT, RANGE, WAIT }
enum class Timeframe { H1, M15, M5 }
enum class ModuleState { DISABLED, SHADOW, ACTIVE }

data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 0.0
)

data class MarketSnapshot(
    val pair: String,
    val candles: List<Candle>,
    val spread: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    val lastPrice: Double get() = candles.lastOrNull()?.close ?: 0.0
}

data class BaselineProposal(
    val signal: Signal,
    val probability: Double,
    val source: String = "FROZEN_BASELINE"
)

data class ModuleObservation(
    val id: String,
    val state: ModuleState,
    val score: Double,
    val veto: Boolean = false,
    val sizeMultiplier: Double = 1.0,
    val note: String = ""
)

data class EngineDecision(
    val pair: String,
    val referencePrice: Double,
    val signal: Signal,
    val confidence: Double,
    val sizeMultiplier: Double,
    val vetoed: Boolean,
    val baseline: BaselineProposal,
    val observations: List<ModuleObservation>,
    val timestamp: Long = System.currentTimeMillis()
)
