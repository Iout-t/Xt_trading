# Forex_MetaEnsemble_RF_V2.3 — real-signal backend artifact

This artifact is a **reconstructed implementation** of the supplied V2.3 handoff. The supplied file explicitly says the full mathematical derivations and implementation details for H–L and N–Q live in a separate consolidated specification, so H–Q here are reconstructed from the roles, dependency order, fixed parameters, and frozen contract stated in the supplied handoff. They are not claimed to be byte-for-byte identical to the missing original implementation.

## Real-signal-only rules

- No synthetic candles, mock market data, random probability fallbacks, or fake broker fills.
- A live signal is emitted only from a fresh broker quote + completed M5/H1/M15 candles + a trained reconstructed baseline model.
- The proposal source remains volatility-band breakout / mean-reversion candidates; no research module can originate a trade.
- The frozen contract is kept: EUR/USD, GBP/USD, USD/JPY; H1/M15 confirmation; M5 execution; triple barriers +1.5x/-1.0x volatility with 24-bar expiry; RF 500 trees, depth 7, min split 50, min leaf 25, sqrt features, entropy, balanced weights, seed 42; p >= 0.55; half-Kelly; max 1.5% risk/trade; max 10x leverage; 2.5% daily drawdown; spread multiplier 3.0; turnover target 0.004.
- H–Q are treated as frozen gates/annotations in reconstructed form. Their behavior is conservative: they can abstain/veto or reduce size, but cannot create a new proposal or raise risk above the frozen cap.
- R–AA and AB–AI remain shadow/audit by default. S can only compose explicitly live A1 restrictions when a human changes the configuration.
- No order-submission endpoint is enabled. This artifact is a signal engine, not an autonomous execution bot.

## Data source

The default adapter targets Twelve Data REST endpoints for EUR/USD, GBP/USD, and USD/JPY candles and quotes. It requests M5, M15, and H1 time series with UTC timestamps and up to 5000 data points per request. Twelve Data does not expose a broker account summary, so position sizing uses the explicitly configured paper-account values `PAPER_ACCOUNT_NAV` and `PAPER_MARGIN_AVAILABLE`. The API key is supplied through the environment; nothing is embedded.

## Run

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
cp config.example.env .env
python -m app.main
```

Or:

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Required for signal mode:

- `TWELVE_DATA_API_KEY`
- `PAPER_ACCOUNT_NAV` (defaults to `10000`)
- `PAPER_MARGIN_AVAILABLE` (defaults to the paper NAV)
- `TWELVE_DATA_SPREAD` (the configured spread estimate used because Twelve Data quote responses do not provide bid/ask)

This is a Twelve Data adaptation of the supplied OANDA-oriented artifact. It is signal-only and never submits orders.

The first live evaluation trains the reconstructed RF from broker history if the cached model is absent. No signal is produced until training succeeds and all required data are fresh.

## API

- `GET /health`
- `GET /config`
- `POST /signal/live`
- `POST /train`

`POST /signal/live` fetches fresh broker data, generates baseline candidates, evaluates H–Q reconstruction gates, and returns either `SIGNAL` or `NO_SIGNAL`.

The response includes an audit trail with data timestamps, model hash, probability, gates, stop/target/expiry, risk size, and a `real_signal_only=true` marker.

## Important implementation note

The handoff itself says two baseline unknowns remain to be confirmed: timeout-label encoding and whether aggregate concurrent/cross-pair exposure is capped elsewhere. This artifact resolves those unknowns only for the reconstructed implementation: timeout is label 0, and aggregate cross-pair exposure is **not silently assumed** because the handoff marks it as an unknown. That is an implementation choice, not a claim about the missing original baseline. The reconstructed engine does not assume an undocumented aggregate cross-pair cap; it enforces the documented per-trade, leverage, daily-drawdown, and spread controls and records the portfolio-cap unknown explicitly.
