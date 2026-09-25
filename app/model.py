import hashlib, json, joblib, numpy as np, pandas as pd
from sklearn.ensemble import RandomForestClassifier
from app.config import MODEL_DIR, BASE
from app.features import candidate_rows, FEATURE_COLS
from app.triplebarrier import label_candidate

class BaselineModel:
    def __init__(self,pair): self.pair=pair; self.path=MODEL_DIR/f'rf_{pair.replace("/","_")}.joblib'
    def fit(self,m5):
        feat,cands=candidate_rows(m5); X=[]; y=[]
        for _,r in cands.iterrows():
            lab,_,_=label_candidate(feat,int(r.idx),int(r.side),float(r.vol),BASE['baseline']['upper_barrier'],BASE['baseline']['lower_barrier'],BASE['baseline']['expiry_bars'])
            vals=[r[c] for c in FEATURE_COLS]
            if all(np.isfinite(vals)): X.append(vals); y.append(lab)
        if len(X)<500 or len(set(y))<2: raise RuntimeError(f'Insufficient trainable candidates for {self.pair}: n={len(X)}')
        rf=RandomForestClassifier(**BASE['baseline']['rf'],n_jobs=-1)
        rf.fit(np.asarray(X,float),np.asarray(y,int))
        payload={'pair':self.pair,'feature_cols':FEATURE_COLS,'spec':BASE['baseline']['rf'],'train_rows':len(X)}
        model_hash=hashlib.sha256(json.dumps(payload,sort_keys=True).encode()).hexdigest(); joblib.dump({'rf':rf,'meta':payload,'model_hash':model_hash},self.path); return model_hash
    def load(self):
        if not self.path.exists(): raise FileNotFoundError(str(self.path))
        d=joblib.load(self.path); return d
    def predict(self,feature_row):
        d=self.load(); rf=d['rf']; cols=d['meta']['feature_cols']; x=np.array([[feature_row[c] for c in cols]],float); p=float(rf.predict_proba(x)[0,1]); return p,d['model_hash']
