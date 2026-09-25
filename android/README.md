# Forex Signals Android APK

This client displays only signals from the remote Python backend. It does not contain broker credentials and it never places orders.

## Backend

Deploy the repository root as a FastAPI service, configure `TWELVE_DATA_API_KEY`, `PAPER_ACCOUNT_NAV`, `PAPER_MARGIN_AVAILABLE`, and `TWELVE_DATA_SPREAD`, then expose HTTPS endpoints including `POST /signal/live`.

## APK build

From this directory:

```bash
gradle assembleDebug -PSIGNAL_BACKEND_URL=https://your-backend.example.com
```

The APK lets the user choose EUR/USD, GBP/USD, or USD/JPY, run a check immediately, and enable Android background checks. Android WorkManager enforces a minimum periodic interval of approximately 15 minutes; the server performs all feature engineering, Random Forest inference, H–Q gates, and paper sizing.
