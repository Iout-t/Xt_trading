import numpy as np

def label_candidate(df, idx, side, vol, up=1.5, dn=1.0, expiry=24):
    entry=float(df.close.iloc[idx]);
    if side==1: upper=entry+up*vol; lower=entry-dn*vol
    else: upper=entry-dn*vol; lower=entry+up*vol
    end=min(len(df)-1,idx+expiry); label=0; exit_idx=end; reason='timeout'
    for j in range(idx+1,end+1):
        hi=float(df.high.iloc[j]); lo=float(df.low.iloc[j])
        if side==1:
            if hi>=upper: label=1; exit_idx=j; reason='upper'; break
            if lo<=lower: label=0; exit_idx=j; reason='lower'; break
        else:
            if lo<=upper: label=1; exit_idx=j; reason='upper'; break
            if hi>=lower: label=0; exit_idx=j; reason='lower'; break
    return label, exit_idx, reason
