import numpy as np, pandas as pd

def rsi(s,n=14):
    d=s.diff(); up=d.clip(lower=0); dn=-d.clip(upper=0)
    au=up.ewm(alpha=1/n,adjust=False).mean(); ad=dn.ewm(alpha=1/n,adjust=False).mean()
    rs=au/(ad+1e-12); return 100-100/(1+rs)

def fracdiff(s,d=0.4,w=20):
    # fixed truncated fractional-difference weights; feature only
    x=s.to_numpy(float); out=np.full(len(x),np.nan); ws=[1.0]
    for k in range(1,w): ws.append(ws[-1]*((d-k+1)/k)*-1)
    for i in range(w-1,len(x)): out[i]=np.dot(np.array(ws[::-1]),x[i-w+1:i+1])
    return pd.Series(out,index=s.index)

def add_features(df):
    x=df.copy(); c=x.close
    ret=np.log(c).diff(); ew=np.sqrt((ret.pow(2).ewm(span=48,min_periods=20).mean()))
    ma=c.rolling(20).mean(); sd=c.rolling(20).std()
    x['fracdiff']=fracdiff(c)
    x['session_sin']=np.sin(2*np.pi*x.time.dt.hour/24); x['session_cos']=np.cos(2*np.pi*x.time.dt.hour/24)
    x['ewma_vol']=ew; x['boll_pos']=(c-ma)/(sd+1e-12); x['boll_bw']=2*sd/(ma.abs()+1e-12)
    x['rsi_norm']=(rsi(c,14)-50)/50
    ema12=c.ewm(span=12,adjust=False).mean(); ema26=c.ewm(span=26,adjust=False).mean(); macd=ema12-ema26; macd_s=macd.ewm(span=9,adjust=False).mean()
    x['macd_std']=(macd-macd_s)/(ew+1e-12)
    x['spread']=x.ask_close-x.bid_close; x['spread_rel']=x['spread']/(c+1e-12)
    x['ret_1']=ret; x['ret_3']=np.log(c).diff(3); x['ret_12']=np.log(c).diff(12)
    return x

FEATURE_COLS=['fracdiff','session_sin','session_cos','ewma_vol','boll_pos','boll_bw','rsi_norm','macd_std','spread','spread_rel','ret_1','ret_3','ret_12','side']

def candidate_rows(df):
    x=add_features(df); out=[]
    for i in range(30,len(x)-24):
        row=x.iloc[i]; vol=float(row.ewma_vol)
        if not np.isfinite(vol) or vol<=0: continue
        # proposal source: volatility-band breakout or mean-reversion candidate
        breakout_long=row.boll_pos>=1.0 and row.rsi_norm>=0
        breakout_short=row.boll_pos<=-1.0 and row.rsi_norm<=0
        mr_long=row.boll_pos<=-1.5 and row.rsi_norm<=-0.2
        mr_short=row.boll_pos>=1.5 and row.rsi_norm>=0.2
        side=1 if (breakout_long or mr_long) else (-1 if (breakout_short or mr_short) else 0)
        if side==0: continue
        rec=row[FEATURE_COLS[:-1]].to_dict(); rec['side']=side; rec['idx']=i; rec['time']=row.time; rec['entry']=row.close; rec['vol']=vol
        out.append(rec)
    return x,pd.DataFrame(out)
