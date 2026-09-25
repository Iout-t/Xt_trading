package com.forex.metaensemble.engine

import com.forex.metaensemble.model.EngineDecision
import com.forex.metaensemble.model.ModuleObservation
import com.forex.metaensemble.model.Signal
import kotlin.math.min

class DecisionEngine {
    fun compose(
        pair: String,
        referencePrice: Double,
        baseline: com.forex.metaensemble.model.BaselineProposal,
        observations: List<ModuleObservation>
    ): EngineDecision {
        val vetoed = observations.any { it.veto }
        val size = observations
            .filter { !it.veto }
            .fold(1.0) { acc, o -> min(acc, o.sizeMultiplier.coerceIn(0.0, 1.0)) }

        val usableScores = observations.filter { !it.veto }.map { it.score }.ifEmpty { listOf(0.5) }
        val adjustment = usableScores.average().let { (it - 0.5) * 0.20 }
        val confidence = (baseline.probability + adjustment).coerceIn(0.0, 1.0)

        val finalSignal = if (vetoed) Signal.WAIT else baseline.signal
        return EngineDecision(
            pair = pair,
            referencePrice = referencePrice,
            signal = finalSignal,
            confidence = confidence,
            sizeMultiplier = size,
            vetoed = vetoed,
            baseline = baseline,
            observations = observations
        )
    }
}
