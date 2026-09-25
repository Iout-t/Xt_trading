import httpx, pandas as pd
from datetime import datetime, timezone
from app.config import TOKEN, ACCOUNT, BASE_URL

PAIR_MAP={'EUR/USD':'EUR_USD','GBP/USD':'GBP_USD','USD/JPY':'USD_JPY'}

class OandaClient:
    def __init__(self):
        if not TOKEN or not ACCOUNT:
            raise RuntimeError('OANDA_API_TOKEN and OANDA_ACCOUNT_ID are required for live signals')
        self.headers={'Authorization':f'Bearer {TOKEN}','Accept-Datetime-Format':'RFC3339'}
    def candles(self,pair,granularity,count=5000):
        inst=PAIR_MAP[pair]
        r=httpx.get(f'{BASE_URL}/v3/accounts/{ACCOUNT}/instruments/{inst}/candles',params={'granularity':granularity,'count':count,'price':'MBA','smooth':'false'},headers=self.headers,timeout=30)
        r.raise_for_status(); rows=[]
        for c in r.json().get('candles',[]):
            if not c.get('complete'): continue
            m=c.get('mid',{}); b=c.get('bid',{}); a=c.get('ask',{})
            if not m: continue
            rows.append({'time':pd.to_datetime(c['time'],utc=True),'open':float(m['o']),'high':float(m['h']),'low':float(m['l']),'close':float(m['c']),'volume':int(c.get('volume',0)),'bid_close':float(b.get('c',m['c'])),'ask_close':float(a.get('c',m['c']))})
        df=pd.DataFrame(rows)
        if df.empty: raise RuntimeError(f'No completed {granularity} candles for {pair}')
        return df.sort_values('time').reset_index(drop=True)
    def price(self,pair):
        inst=PAIR_MAP[pair]
        r=httpx.get(f'{BASE_URL}/v3/accounts/{ACCOUNT}/pricing',params={'instruments':inst,'includeUnitsAvailable':'false'},headers=self.headers,timeout=15)
        r.raise_for_status(); p=r.json()['prices'][0]
        if not p.get('tradeable',False): raise RuntimeError(f'{pair} is not tradeable now')
        bid=float(p['bids'][0]['price']); ask=float(p['asks'][0]['price'])
        return {'time':pd.to_datetime(p['time'],utc=True),'bid':bid,'ask':ask,'mid':(bid+ask)/2,'tradeable':True}
    def account_summary(self):
        r=httpx.get(f'{BASE_URL}/v3/accounts/{ACCOUNT}/summary',headers=self.headers,timeout=15); r.raise_for_status(); return r.json()['account']
