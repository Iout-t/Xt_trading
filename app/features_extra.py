# Shadow-only AB-AI helpers. These are never called as proposal generators.
import numpy as np

def dc_activity(mid,threshold):
    m=np.asarray(mid,float); count=0; direction=0; last=m[0]
    for p in m[1:]:
        move=(p-last)/(last+1e-12)
        if direction<=0 and move>=threshold: direction=1; count+=1; last=p
        elif direction>=0 and move<=-threshold: direction=-1; count+=1; last=p
    return {'dc_direction':direction,'dc_event_count':count,'event_intensity':count/max(1,len(m)-1)}

def tri_residual(eurusd,gbpusd,eurgbp): return float(np.log(eurusd)-np.log(gbpusd)-np.log(eurgbp))
def quote_ofi(dbid,dask):
    den=abs(dbid)+abs(dask); return 0.0 if den==0 else float((dbid-dask)/den)
def post_event_state(surprise_z,ret_impulse,vol_impulse):
    if abs(vol_impulse)>2 and abs(ret_impulse)<0.5:return 'volatility_spike_without_direction'
    if abs(surprise_z)>1 and abs(ret_impulse)>1:return 'directional_shock'
    if abs(ret_impulse)<0.25:return 'rapid_absorption'
    return 'mixed_response'
