# AB-AI shadow-only annotations; imported separately so they cannot create proposals.
import numpy as np

def AB_dc(mid, threshold):
    m=np.asarray(mid,float); last=m[0]; d=0; n=0
    for p in m[1:]:
        z=(p-last)/(last+1e-12)
        if d<=0 and z>=threshold: d=1; n+=1; last=p
        elif d>=0 and z<=-threshold: d=-1; n+=1; last=p
    return {'dc_direction':d,'dc_event_count':n,'event_intensity':n/max(1,len(m)-1)}

def AC_triangle(eurusd,gbpusd,eurgbp): return float(np.log(eurusd)-np.log(gbpusd)-np.log(eurgbp))
def AD_ofi(dbid,dask):
    den=abs(dbid)+abs(dask); return 0.0 if den==0 else float((dbid-dask)/den)
def AI_state(surprise_z,ret_impulse,vol_impulse):
    if abs(vol_impulse)>2 and abs(ret_impulse)<.5: return 'volatility_spike_without_direction'
    if abs(surprise_z)>1 and abs(ret_impulse)>1: return 'directional_shock'
    if abs(ret_impulse)<.25: return 'rapid_absorption'
    return 'mixed_response'
