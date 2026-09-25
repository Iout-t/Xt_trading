import numpy as np, pandas as pd
from scipy.stats import norm

def path_features(m5):
    r=np.log(m5.close).diff().dropna().tail(32).to_numpy()
    if len(r)<10: return {'h_score':0.0,'lead_lag':0.0}
    # Reconstructed truncated signature-like moments: first level, signed area, quadratic variation.
    path=np.cumsum(r); area=np.sum(path[:-1]*r[1:]-np.cumsum(r[1:])*r[:-1])
    qv=float(np.sum(r*r)); lead=float(np.corrcoef(r[:-1],r[1:])[0,1]) if len(r)>2 else 0.0
    h=np.tanh((np.mean(r)/(np.std(r)+1e-12))*2 + np.tanh(area/(qv+1e-12))*.5)
    return {'h_score':float(h),'lead_lag':float(np.nan_to_num(lead))}

def hawkes_branching(m5,window=120):
    x=np.log(m5.close).diff().dropna().tail(window); thr=max(1e-9,float(x.abs().median())*3.0); ev=(x.abs()>thr).astype(int).to_numpy()
    if ev.sum()<5: return 0.0
    lam=ev.mean(); ac=np.corrcoef(ev[:-1],ev[1:])[0,1] if ev.sum()>2 else 0.0
    n=float(np.clip(lam*(1+max(ac,0))*10,0,0.999))
    return n

def regime_state(m5):
    r=np.log(m5.close).diff().dropna().tail(64); v=float(r.std()); vv=float(r.ewm(span=16).std().iloc[-1] or v); spread=float((m5.ask_close-m5.bid_close).tail(20).mean())
    if v>0.0009 or vv>0.0008: return 'high_vol_reflexive'
    if spread>np.nanmedian(m5.ask_close-m5.bid_close)*1.5: return 'liquidity_stress'
    return 'low_vol_exogenous'

def jump_drift(m5):
    r=np.log(m5.close).diff().dropna().tail(24); mu=float(r.mean()); sig=float(r.std()+1e-12); z=mu/sig
    jump=abs(float(r.iloc[-1]))>3*sig if len(r) else False
    return {'drift_z':z,'jump':jump}

def conformal_interval_width(p_history):
    if len(p_history)<30: return 0.20
    return float(np.quantile(np.abs(np.asarray(p_history)-0.5),0.9))*0.5+0.05

def hq_gates(m5,side,p,p_history=None):
    a=path_features(m5); n=hawkes_branching(m5); reg=regime_state(m5); jd=jump_drift(m5); width=conformal_interval_width(p_history or [])
    # J: reconstructed interval calibration/abstention: wider uncertainty -> abstain.
    lo=max(0.0,p-width); hi=min(1.0,p+width)
    j_veto=(hi-lo>0.38) or (lo<0.50 and hi>0.50 and p<0.58)
    # K: hard circuit breaker at n > 0.95.
    k_veto=n>0.95
    # H: secondary path vote. Strong contradiction abstains.
    h_dir=1 if a['h_score']>0.08 else (-1 if a['h_score']<-0.08 else 0)
    h_veto=(h_dir!=0 and h_dir!=side and abs(a['h_score'])>0.35)
    # N: regime classifier, annotation only; Q/O use it.
    # I/O: reconstructed jump/drift veto. Conservative and only for strong adverse evidence.
    adverse=(jd['drift_z']*side)<-1.75 and abs(jd['drift_z'])>1.75
    i_veto=bool(jd['jump'] and adverse)
    o_veto=bool(adverse and reg=='high_vol_reflexive')
    # Q: Hawkes refined timing: hard veto in reflexive cascade.
    q_veto=bool(n>0.85 and reg=='high_vol_reflexive' and abs(a['h_score'])<0.15)
    # L/P: dynamic size-down. This reconstruction never exceeds baseline size.
    es_mult=float(np.clip(1.0 - max(0,n-0.55)*1.2 - max(0,width-0.18)*1.5,0.15,1.0))
    if reg=='liquidity_stress': es_mult=min(es_mult,0.5)
    return {'veto':any([j_veto,k_veto,h_veto,i_veto,o_veto,q_veto]),'size_multiplier':es_mult,
            'details':{'H':a,'J':{'lo':lo,'hi':hi,'veto':j_veto},'K':{'branching_ratio':n,'veto':k_veto},'N':{'regime':reg},'I':i_veto,'O':o_veto,'Q':q_veto,'L_P_size_multiplier':es_mult}}
