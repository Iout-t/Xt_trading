from pathlib import Path
import os
from dotenv import load_dotenv
load_dotenv()

BASE = {
    'profile':'Forex_MetaEnsemble_RF_V2.3_Additive', 'version':'2.3.0-reconstructed',
    'universe':['EUR/USD','GBP/USD','USD/JPY'],
    'granularities':{'entry':'M5','structure':'M15','macro':'H1'},
    'baseline':{
        'p_threshold':0.55,'upper_barrier':1.5,'lower_barrier':1.0,'expiry_bars':24,
        'rf':{'n_estimators':500,'max_depth':7,'min_samples_split':50,'min_samples_leaf':25,'max_features':'sqrt','criterion':'entropy','class_weight':'balanced','random_state':42},
        'validation':{'method':'CPCV_reconstructed','splits':6,'test_splits':2,'embargo_bars':50},
    },
    'risk':{'half_kelly':True,'max_trade_risk':0.015,'max_leverage':10.0,'daily_drawdown':0.025,'spread_multiplier':3.0,'turnover_target':0.004,'max_usd_factor_risk':0.03},
}
MODULES={
'H':{'enabled':True,'flag':'path_signature_features_v1'},'I':{'enabled':True,'flag':'neural_jump_ode_execution_v1'},
'J':{'enabled':True,'flag':'venn_abers_calibration_v1'},'K':{'enabled':True,'flag':'hawkes_branching_sentinel_v1'},
'L':{'enabled':True,'flag':'conformal_cvar_control_v1'},'N':{'enabled':True,'flag':'signature_regime_classifier_v1'},
'O':{'enabled':True,'flag':'jump_drift_veto_v1'},'P':{'enabled':True,'flag':'epistemic_weighted_cvar_sizer_v1'},
'Q':{'enabled':True,'flag':'hawkes_refined_m5_veto_v1'},
'R':{'enabled':False,'mode':'shadow','flag':'anytime_valid_drift_sentinel_v1'},'S':{'enabled':False,'mode':'shadow','flag':'composite_decision_governor_v1'},
'T':{'enabled':False,'mode':'shadow','flag':'event_liquidity_calendar_gate_v1'},'U':{'enabled':False,'mode':'shadow','flag':'competing_risks_net_ev_gate_v1'},
'V':{'enabled':False,'mode':'shadow','flag':'currency_factor_netting_v1'},'W':{'enabled':False,'mode':'shadow','flag':'persistent_regime_ledger_v1'},
'X':{'enabled':False,'mode':'shadow','flag':'foundation_model_text_annotator_v1'},'Y':{'enabled':True,'mode':'governance','flag':'promotion_statistics_harness_v1'},
'Z':{'enabled':True,'mode':'governance','flag':'fill_realism_stress_harness_v1'},'AA':{'enabled':False,'mode':'shadow','flag':'cross_asset_context_v1'},
'AB':{'enabled':False,'mode':'shadow','flag':'event_time_directional_change_v1'},'AC':{'enabled':False,'mode':'shadow','flag':'triangular_consistency_residual_v1'},
'AD':{'enabled':False,'mode':'shadow','flag':'order_flow_imbalance_causal_screen_v1'},'AE':{'enabled':False,'mode':'shadow','flag':'conditional_return_distribution_v1'},
'AF':{'enabled':False,'mode':'shadow','flag':'online_expert_aggregation_v1'},'AG':{'enabled':False,'mode':'shadow','flag':'causal_veto_value_auditor_v1'},
'AH':{'enabled':False,'mode':'shadow','flag':'size_dependent_capacity_surface_v1'},'AI':{'enabled':False,'mode':'shadow','flag':'post_announcement_surprise_response_v1'} }

MODEL_DIR=Path(os.getenv('MODEL_DIR','./models'))
MODEL_DIR.mkdir(parents=True,exist_ok=True)
TWELVE_DATA_API_KEY=os.getenv('TWELVE_DATA_API_KEY','')
TWELVE_DATA_BASE_URL=os.getenv('TWELVE_DATA_BASE_URL','https://api.twelvedata.com').rstrip('/')
PAPER_ACCOUNT_NAV=float(os.getenv('PAPER_ACCOUNT_NAV','10000'))
PAPER_MARGIN_AVAILABLE=float(os.getenv('PAPER_MARGIN_AVAILABLE',str(PAPER_ACCOUNT_NAV)))
TWELVE_DATA_SPREAD=float(os.getenv('TWELVE_DATA_SPREAD','0.0001'))
LOOKBACK_M5=int(os.getenv('LOOKBACK_M5','5000')); LOOKBACK_M15=int(os.getenv('LOOKBACK_M15','1200')); LOOKBACK_H1=int(os.getenv('LOOKBACK_H1','600'))
