import pandas as pd, numpy as np
from app.features import add_features,candidate_rows

def frame(n=200):
    t=pd.date_range('2026-01-01',periods=n,freq='5min',tz='UTC'); p=1.1+np.cumsum(np.random.default_rng(4).normal(0,0.0002,n))
    return pd.DataFrame({'time':t,'open':p,'high':p+0.0002,'low':p-0.0002,'close':p,'volume':1000,'bid_close':p-0.00005,'ask_close':p+0.00005})

def test_features_exist():
    d=add_features(frame()); assert {'ewma_vol','boll_pos','rsi_norm','macd_std','spread'}.issubset(d.columns)

def test_candidates_do_not_fail():
    _, c=candidate_rows(frame()); assert isinstance(c,pd.DataFrame)
