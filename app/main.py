from fastapi import FastAPI,HTTPException
from pydantic import BaseModel
from app.engine import live_signal,train_pair
from app.config import BASE,MODULES

app=FastAPI(title='Forex_MetaEnsemble_RF_V2.3 Real Signal Backend',version='2.3-reconstructed')
class SignalReq(BaseModel): pair:str

@app.get('/')
def root(): return {'status':'ok','service':'Forex signals backend','health':'/health','signal_endpoint':'POST /signal/live','real_signal_only':True}

@app.get('/health')
def health(): return {'status':'ok','real_signal_only':True,'version':'2.3-reconstructed'}
@app.get('/config')
def config(): return {'base':BASE,'modules':MODULES}
@app.post('/train')
def train(req:SignalReq):
    try: return {'status':'TRAINED','pair':req.pair,'model_hash':train_pair(req.pair)}
    except Exception as e: raise HTTPException(500,str(e))
@app.post('/signal/live')
def signal(req:SignalReq):
    try: return live_signal(req.pair)
    except Exception as e: raise HTTPException(503,str(e))
