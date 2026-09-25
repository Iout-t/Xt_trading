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
