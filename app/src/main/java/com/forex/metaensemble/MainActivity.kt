package com.forex.metaensemble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.forex.metaensemble.ui.MetaEnsembleApp
import com.forex.metaensemble.ui.MetaEnsembleViewModel
import com.forex.metaensemble.ui.theme.MetaEnsembleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MetaEnsembleTheme {
                MetaEnsembleApp(viewModel())
            }
        }
    }
}
