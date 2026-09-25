# Forex MetaEnsemble V2.3 — Kotlin Android

A Kotlin/Jetpack Compose Android research and paper-signal application for the additive MetaEnsemble specification.

## What is included

- Kotlin Android app with Jetpack Compose UI.
- Explicit `BaselineAdapter` boundary for the frozen V1.2/H-Q baseline.
- Demo baseline adapter clearly marked as a placeholder; it does **not** claim to reproduce the trained Random Forest.
- AB–AI modules from the V2.3 additive research handoff.
- AB–AI are shadow-only by default.
- Deterministic decision composition.
- Basic risk guard using the V2.2 contract limits.
- Simulated market feed for safe local testing.
- Unit test for engine output.
- GitHub Actions Android build workflow.
- Strategy JSON carrying the frozen contract values and module states.

## Important

This project is a research/paper-mode app. It does not place live trades.

The supplied source specification explicitly says that the frozen baseline must not be modified and that new modules must not originate trades. The actual frozen RF artifact/data feed must be connected through `BaselineAdapter` after it has been verified.

The AB–AI implementations in this starter are conservative software interfaces/proxies, not claims of empirical trading performance.

## Android build

Open the folder in current Android Studio and let Gradle sync.

Or on a machine with Gradle installed:

```bash
gradle :app:assembleDebug
```

The GitHub Actions workflow installs Gradle and builds the debug APK.

## GitHub

Create a repository, upload the contents of this directory, then open:

Actions → Android Build

The workflow artifact is `app-debug`.

## Next integration steps

1. Replace `DemoBaselineAdapter` with the verified frozen baseline implementation.
2. Connect a point-in-time market-data provider.
3. Add historical data replay tests.
4. Implement the full acceptance gates for AB–AI.
5. Keep AB–AI shadow-only until pre-registered evaluation passes.
6. Add a separate, explicitly enabled live-execution layer only after paper validation and human approval.

Never put broker API keys directly in the repository.

## Live market signal mode

The app now supports live analytical signals for provider-supported forex and cryptocurrency instruments through a secure backend proxy. The Android client does not contain the Twelve Data key. It loads the provider instrument catalog, including pairs such as `USD/AUD` and `BTC/USD` when available under the account plan, and polls the proxy every 30 seconds while live updates are enabled.

The proxy is in [`server/`](server/). Configure the secret only in the deployment environment:

```bash
export TWELVE_DATA_API_KEY="your-secret"
node server/server.js
```

Do not commit the real key, place it in an APK, or put it in source files. The GitHub Actions repository secret should be named `TWELVE_DATA_API_KEY`; the running backend must expose the same environment variable. The Android app defaults to `http://10.0.2.2:8787` for an Android emulator. For a deployed HTTPS proxy, build with:

```bash
gradle -PSIGNAL_PROXY_URL=https://your-proxy.example.com :app:assembleDebug
```

The proxy exposes:

- `GET /health` for availability checks.
- `GET /api/instruments` for the normalized forex and crypto catalog.
- `GET /api/market?symbol=USD%2FAUD&interval=15min` for candles and the current reference price.

Signals remain **analysis only**. The app does not connect to a broker, place orders, transmit trades, or claim guaranteed performance. Validate data freshness, subscription access, spread, liquidity, and risk independently before acting on any signal.

### Android cleartext note

The debug app currently permits cleartext traffic so an Android emulator can reach the local development proxy at `http://10.0.2.2:8787`. Use an HTTPS proxy URL for a physical device or production deployment and remove `android:usesCleartextTraffic="true"` from the manifest before a production release.

On a physical Android phone, open the app’s **Signal proxy URL** field and enter the LAN URL of the computer running the backend, for example `http://192.168.1.25:8787`, then tap **Apply proxy URL**. The phone and computer must be on the same Wi-Fi network, and the backend must be running on `0.0.0.0:8787`.

### Hosted proxy

The Android 2.3.2 build defaults to the managed HTTPS proxy checkpoint at `https://3000-iiygy1e5g54v6vskrnnb1-8b383e2c.sg2.manus.computer`. The Twelve Data key remains server-side. The app’s proxy URL field can override this endpoint if the hosted URL changes.
