from datetime import datetime,timezone
import numpy as np
from app.config import BASE, MODULES
from app.oanda import OandaClient
from app.features import add_features,candidate_rows,FEATURE_COLS
from app.model import BaselineModel
from app.hq import hq_gates

def fresh_client(): return OandaClient()

def _size_units(account,pair,entry,stop,risk_frac,leverage_cap):
    nav=float(account.get('NAV',account.get('balance',0))); risk=max(0.0,nav*risk_frac); distance=abs(entry-stop)
    if risk<=0 or distance<=0:return 0
    units=risk/distance
    max_units=max(0.0,abs(float(account.get('marginAvailable',nav)))*leverage_cap/entry)
    return int(min(units,max_units))

def train_pair(pair,client=None):
    c=client or fresh_client(); m5=c.candles(pair,'M5',5000); model=BaselineModel(pair); return model.fit(m5)

def live_signal(pair,client=None):
    if pair not in BASE['universe']: raise ValueError('pair not in frozen universe')
    c=client or fresh_client(); model=BaselineModel(pair)
    m5=c.candles(pair,'M5',5000); m15=c.candles(pair,'M15',1200); h1=c.candles(pair,'H1',600); px=c.price(pair); acct=c.account_summary()
    # fresh quote rule: quote must be recent relative to decision timestamp
    now=datetime.now(timezone.utc); age=(now-px['time'].to_pydatetime()).total_seconds()
    if age>15: return {'status':'NO_SIGNAL','reason':'stale_quote','audit':{'quote_age_seconds':age,'real_signal_only':True}}
    # confirmations: reconstructed H1/M15 trend alignment around the latest completed bar
    def trend(df,n=12):
        r=np.log(df.close).diff(n).iloc[-1]; return 1 if r>0 else (-1 if r<0 else 0)
    hdir=trend(h1,12); mdir=trend(m15,12)
    x,cands=candidate_rows(m5)
    latest=[r for _,r in cands.iterrows() if r['idx']==len(x)-1]
    if not latest: return {'status':'NO_SIGNAL','reason':'no_frozen_proposal_now','audit':{'real_signal_only':True}}
    base=latest[-1].to_dict(); side=int(base['side'])
    if hdir!=side or mdir!=side: return {'status':'NO_SIGNAL','reason':'H1_M15_confirmation_missing','audit':{'h1':hdir,'m15':mdir,'real_signal_only':True}}
    p,model_hash=model.predict(base)
    if p<BASE['baseline']['p_threshold']: return {'status':'NO_SIGNAL','reason':'p_below_0.55','audit':{'p':p,'model_hash':model_hash,'real_signal_only':True}}
    spread=px['ask']-px['bid']; spread_hist=(m5.ask_close-m5.bid_close).tail(240); med=float(spread_hist.median());
    if med<=0 or spread>med*BASE['risk']['spread_multiplier']: return {'status':'NO_SIGNAL','reason':'spread_guard','audit':{'spread':spread,'median_spread':med,'real_signal_only':True}}
    gates=hq_gates(m5,side,p)
    if gates['veto']: return {'status':'NO_SIGNAL','reason':'frozen_HQ_gate_veto','audit':{'p':p,'model_hash':model_hash,'gates':gates,'real_signal_only':True}}
    mid=px['mid']; vol=float(base['vol']);
    if side==1: stop=mid-BASE['baseline']['lower_barrier']*vol; target=mid+BASE['baseline']['upper_barrier']*vol
    else: stop=mid+BASE['baseline']['lower_barrier']*vol; target=mid-BASE['baseline']['upper_barrier']*vol
    mult=gates['size_multiplier']
    # half-Kelly for reward:risk 1.5, then apply H-Q downscale; never raise above cap
    rr=1.5; q=1-p; k=((p*rr-q)/rr)*0.5; risk_frac=min(BASE['risk']['max_trade_risk'],max(0,k))*mult
    units=_size_units(acct,pair,mid,stop,risk_frac,BASE['risk']['max_leverage'])
    if units<=0: return {'status':'NO_SIGNAL','reason':'zero_safe_position_size','audit':{'risk_fraction':risk_frac,'real_signal_only':True}}
    return {'status':'SIGNAL','reason':'live_frozen_baseline_plus_reconstructed_HQ_passed','signal':{
      'pair':pair,'side':'BUY' if side==1 else 'SELL','entry_reference':mid,'bid':px['bid'],'ask':px['ask'],'probability':p,
      'stop':stop,'target':target,'expiry_m5_bars':BASE['baseline']['expiry_bars'],'risk_fraction':risk_frac,'units':units,
      'signal_source':'frozen_event_generator+RF_meta_label','model_hash':model_hash,'simulated':False},
      'audit':{'quote_time':px['time'].isoformat(),'model_hash':model_hash,'gates':gates,'real_signal_only':True,'research_modules':'shadow/audit_only'}}
