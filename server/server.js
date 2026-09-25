const http = require('node:http');
const { URL } = require('node:url');

const PORT = Number(process.env.PORT || 8787);
const TWELVE_DATA_API_KEY = process.env.TWELVE_DATA_API_KEY;
const TWELVE_DATA_BASE_URL = 'https://api.twelvedata.com';
const CACHE_TTL_MS = 5 * 60 * 1000;
const instrumentsCache = { expiresAt: 0, data: [] };

function json(res, status, body) {
  const payload = JSON.stringify(body);
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(payload),
    'Access-Control-Allow-Origin': process.env.ALLOW_ORIGIN || '*',
    'Cache-Control': 'no-store'
  });
  res.end(payload);
}

function normalizeSymbol(raw) {
  const symbol = String(raw.symbol || raw.instrument || '').trim();
  const currency = String(raw.currency || '').trim();
  const name = String(raw.name || symbol).trim();
  const type = String(raw.type || '').toLowerCase();
  const isCrypto = type.includes('digital') || type.includes('crypto') || String(raw.asset_class || '').toLowerCase().includes('crypto');
  return {
    symbol,
    displayName: name,
    assetClass: isCrypto ? 'crypto' : 'forex',
    currency,
    exchange: String(raw.exchange || raw.exchange_timezone || '').trim()
  };
}

async function twelveData(path, params = {}) {
  if (!TWELVE_DATA_API_KEY) throw new Error('TWELVE_DATA_API_KEY is not configured on the server');
  const url = new URL(`${TWELVE_DATA_BASE_URL}${path}`);
  Object.entries(params).forEach(([key, value]) => url.searchParams.set(key, String(value)));
  const response = await fetch(url, { headers: { Authorization: `apikey ${TWELVE_DATA_API_KEY}` } });
  const body = await response.json();
  if (!response.ok || body.status === 'error') {
    throw new Error(body.message || `Twelve Data returned HTTP ${response.status}`);
  }
  return body;
}

async function loadInstruments() {
  if (Date.now() < instrumentsCache.expiresAt) return instrumentsCache.data;
  const [forex, crypto] = await Promise.all([
    twelveData('/forex_pairs'),
    twelveData('/cryptocurrencies', { show_plan: false })
  ]);
  const forexRows = Array.isArray(forex.data) ? forex.data : Array.isArray(forex) ? forex : [];
  const cryptoRows = Array.isArray(crypto.data) ? crypto.data : Array.isArray(crypto) ? crypto : [];
  const data = [...forexRows, ...cryptoRows]
    .map(normalizeSymbol)
    .filter((item) => item.symbol)
    .filter((item, index, all) => all.findIndex((candidate) => candidate.symbol === item.symbol) === index)
    .sort((a, b) => `${a.assetClass}:${a.symbol}`.localeCompare(`${b.assetClass}:${b.symbol}`));
  instrumentsCache.data = data;
  instrumentsCache.expiresAt = Date.now() + CACHE_TTL_MS;
  return data;
}

function parseTimeSeries(body, symbol) {
  const values = Array.isArray(body.values) ? body.values : [];
  return values.reverse().map((row) => ({
    timestamp: Date.parse(row.datetime) || Date.now(),
    open: Number(row.open),
    high: Number(row.high),
    low: Number(row.low),
    close: Number(row.close),
    volume: Number(row.volume || 0)
  })).filter((candle) => Number.isFinite(candle.close));
}

function normalizeRequestedSymbol(symbol) {
  const value = String(symbol || '').trim().toUpperCase().replace(/\s+/g, '');
  if (value === 'BTCUSD') return 'BTC/USD';
  if (/^[A-Z]{6}$/.test(value)) return `${value.slice(0, 3)}/${value.slice(3)}`;
  return value;
}

async function loadMarket(symbol, interval) {
  const providerSymbol = normalizeRequestedSymbol(symbol);
  const [series, quote] = await Promise.all([
    twelveData('/time_series', { symbol: providerSymbol, interval, outputsize: 100, timezone: 'UTC' }),
    twelveData('/quote', { symbol: providerSymbol })
  ]);
  const candles = parseTimeSeries(series, providerSymbol);
  if (!candles.length) throw new Error(`No candle data returned for ${providerSymbol}`);
  const price = Number(quote.close || quote.price || candles[candles.length - 1].close);
  const bid = Number(quote.bid || 0);
  const ask = Number(quote.ask || 0);
  return {
    pair: providerSymbol,
    interval,
    price,
    spread: ask > 0 && bid > 0 ? Math.max(ask - bid, 0) : 0,
    timestamp: Date.now(),
    candles
  };
}

async function handler(req, res) {
  const requestUrl = new URL(req.url, `http://${req.headers.host}`);
  if (req.method === 'OPTIONS') return json(res, 204, {});
  if (req.method !== 'GET') return json(res, 405, { status: 'error', message: 'GET only' });
  try {
    if (requestUrl.pathname === '/health') return json(res, 200, { status: 'ok', provider: 'twelve-data' });
    if (requestUrl.pathname === '/api/instruments') return json(res, 200, { status: 'ok', data: await loadInstruments() });
    if (requestUrl.pathname === '/api/market') {
      const symbol = requestUrl.searchParams.get('symbol');
      const interval = requestUrl.searchParams.get('interval') || '15min';
      if (!symbol) return json(res, 400, { status: 'error', message: 'symbol is required' });
      return json(res, 200, { status: 'ok', data: await loadMarket(symbol, interval) });
    }
    return json(res, 404, { status: 'error', message: 'Not found' });
  } catch (error) {
    console.error(error.message);
    return json(res, 502, { status: 'error', message: error.message });
  }
}

if (require.main === module) {
  if (!TWELVE_DATA_API_KEY) console.warn('TWELVE_DATA_API_KEY is not set; provider requests will fail safely.');
  http.createServer(handler).listen(PORT, '0.0.0.0', () => console.log(`Signal proxy listening on :${PORT}`));
}

module.exports = { handler, normalizeSymbol, parseTimeSeries };
