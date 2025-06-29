package com.example.projeto_ttc2.presentation.ui.screen

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import com.example.projeto_ttc2.presentation.MainActivity
import com.example.projeto_ttc2.presentation.ui.theme.ProjetoTTC2Theme
import com.example.projeto_ttc2.presentation.viewmodel.HealthConnectViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    private val healthConnectViewModel: HealthConnectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            healthConnectViewModel.initializeRepository(this@SplashActivity)
            if (healthConnectViewModel.hasAllPermissions()) {
                healthConnectViewModel.syncData().join()
                navigateToMain()
            } else {
                showPermissionScreen()
            }
        }
    }

    private fun showPermissionScreen() {
        val requestPermission = registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { granted: Set<String> ->

            healthConnectViewModel.onPermissionsResult(granted)
            navigateToMain()
        }


        lifecycleScope.launch {
            healthConnectViewModel.permissionRequestChannel.collect { permissions ->
                requestPermission.launch(permissions)
            }
        }

        setContent {
            ProjetoTTC2Theme {
                SplashScreen(
                    onContinueClick = {
                        healthConnectViewModel.initialLoad(this)
                    }
                )
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}