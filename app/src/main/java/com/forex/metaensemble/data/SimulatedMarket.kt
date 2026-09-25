package com.forex.metaensemble.data

import com.forex.metaensemble.model.Candle
import com.forex.metaensemble.model.MarketSnapshot
import kotlin.math.sin
import kotlin.random.Random

class SimulatedMarket {
    private var step = 0
    private var price = 1.1000

    fun next(pair: String = "EUR/USD"): MarketSnapshot {
        step++
        val drift = sin(step / 17.0) * 0.00008
        val noise = Random.nextDouble(-0.00012, 0.00012)
        val open = price
        val close = (price + drift + noise).coerceAtLeast(0.0001)
        val high = maxOf(open, close) + Random.nextDouble(0.0, 0.00008)
        val low = minOf(open, close) - Random.nextDouble(0.0, 0.00008)
        price = close

        val candles = (0 until 60).map { i ->
            val p = price + sin((step - 60 + i) / 17.0) * 0.0005
            Candle(
                timestamp = System.currentTimeMillis() - (59 - i) * 5 * 60_000L,
                open = p,
                high = p + 0.0001,
                low = p - 0.0001,
                close = p + sin((step - 60 + i) / 5.0) * 0.00005,
                volume = Random.nextDouble(100.0, 1000.0)
            )
        } + Candle(System.currentTimeMillis(), open, high, low, close, Random.nextDouble(100.0, 1000.0))

        return MarketSnapshot(pair, candles, spread = 0.00012)
    }
}
