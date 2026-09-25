package com.forex.metaensemble.data

import com.forex.metaensemble.model.Candle
import com.forex.metaensemble.model.MarketSnapshot
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

class LiveMarketClient(private val baseUrl: String) {
    data class Instrument(val symbol: String, val displayName: String, val assetClass: String)

    fun instruments(): List<Instrument> {
        val root = JSONObject(get("/api/market-data/instruments"))
        val rows = root.optJSONArray("data") ?: JSONArray()
        return buildList {
            for (index in 0 until rows.length()) {
                val row = rows.getJSONObject(index)
                add(Instrument(row.optString("symbol"), row.optString("displayName"), row.optString("assetClass")))
            }
        }.filter { it.symbol.isNotBlank() }
    }

    fun market(symbol: String, interval: String = "15min"): MarketSnapshot {
        val path = "/api/market-data/market?symbol=${encode(symbol)}&interval=${encode(interval)}"
        val data = JSONObject(get(path)).getJSONObject("data")
        val candlesJson = data.getJSONArray("candles")
        val candles = buildList {
            for (index in 0 until candlesJson.length()) {
                val row = candlesJson.getJSONObject(index)
                add(Candle(
                    timestamp = row.optLong("timestamp"),
                    open = row.optDouble("open"),
                    high = row.optDouble("high"),
                    low = row.optDouble("low"),
                    close = row.optDouble("close"),
                    volume = row.optDouble("volume", 0.0)
                ))
            }
        }
        return MarketSnapshot(
            pair = data.getString("pair"),
            candles = candles,
            spread = data.optDouble("spread", 0.0),
            timestamp = data.optLong("timestamp")
        )
    }

    private fun get(path: String): String {
        val connection = (URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 20_000
            setRequestProperty("Accept", "application/json")
        }
        return try {
            val responseCode = connection.responseCode
            val body = (if (responseCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { reader -> reader.readText() }
                ?: "{}"
            if (responseCode !in 200..299) {
                throw IllegalStateException(JSONObject(body).optString("message", "Signal proxy request failed"))
            }
            body
        } finally {
            connection.disconnect()
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())
}
