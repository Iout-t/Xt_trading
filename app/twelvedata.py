from datetime import datetime, timezone

import httpx
import pandas as pd

from app.config import (
    PAPER_ACCOUNT_NAV,
    PAPER_MARGIN_AVAILABLE,
    TWELVE_DATA_API_KEY,
    TWELVE_DATA_BASE_URL,
    TWELVE_DATA_SPREAD,
)


PAIR_MAP = {"EUR/USD": "EUR/USD", "GBP/USD": "GBP/USD", "USD/JPY": "USD/JPY"}
INTERVAL_MAP = {"M5": "5min", "M15": "15min", "H1": "1h"}


class TwelveDataClient:
    """Twelve Data adapter for the signal-only, paper-sizing backend."""

    def __init__(self):
        if not TWELVE_DATA_API_KEY:
            raise RuntimeError("TWELVE_DATA_API_KEY is required for live signals")
        self.client = httpx.Client(base_url=TWELVE_DATA_BASE_URL, timeout=30)

    def _get(self, path, **params):
        response = self.client.get(path, params={**params, "apikey": TWELVE_DATA_API_KEY})
        response.raise_for_status()
        payload = response.json()
        if payload.get("status") == "error" or payload.get("code"):
            raise RuntimeError(payload.get("message", "Twelve Data request failed"))
        return payload

    def candles(self, pair, granularity, count=5000):
        if pair not in PAIR_MAP:
            raise ValueError(f"unsupported pair: {pair}")
        payload = self._get(
            "/time_series",
            symbol=PAIR_MAP[pair],
            interval=INTERVAL_MAP[granularity],
            outputsize=min(count, 5000),
            order="asc",
            timezone="UTC",
        )
        rows = []
        for candle in payload.get("values", []):
            close = float(candle["close"])
            rows.append(
                {
                    "time": pd.to_datetime(candle["datetime"], utc=True),
                    "open": float(candle["open"]),
                    "high": float(candle["high"]),
                    "low": float(candle["low"]),
                    "close": close,
                    "volume": int(float(candle.get("volume") or 0)),
                    "bid_close": close - TWELVE_DATA_SPREAD / 2,
                    "ask_close": close + TWELVE_DATA_SPREAD / 2,
                }
            )
        frame = pd.DataFrame(rows)
        if frame.empty:
            raise RuntimeError(f"No completed {granularity} candles for {pair}")
        return frame.sort_values("time").reset_index(drop=True)

    def price(self, pair):
        payload = self._get("/quote", symbol=PAIR_MAP[pair])
        mid = float(payload.get("close") or payload.get("price"))
        timestamp = payload.get("timestamp")
        when = (
            pd.to_datetime(timestamp, unit="s", utc=True)
            if timestamp
            else pd.Timestamp.now(tz="UTC")
        )
        return {
            "time": when,
            "bid": mid - TWELVE_DATA_SPREAD / 2,
            "ask": mid + TWELVE_DATA_SPREAD / 2,
            "mid": mid,
            "tradeable": bool(payload.get("is_market_open", True)),
        }

    def account_summary(self):
        return {"NAV": PAPER_ACCOUNT_NAV, "marginAvailable": PAPER_MARGIN_AVAILABLE}

    def close(self):
        self.client.close()


def utc_now():
    return datetime.now(timezone.utc)
EOF
rm -f /home/ubuntu/Xt_trading/app/oanda.py
