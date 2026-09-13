package com.dev2drop.cleanring

import android.Manifest
import android.app.role.RoleManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.dev2drop.cleanring.billing.BillingManager
import com.dev2drop.cleanring.service.ProtectionService
import com.dev2drop.cleanring.ui.SpamBlockerHomeScreen
import com.dev2drop.cleanring.ui.SpamBlockerViewModel
import com.dev2drop.cleanring.ui.theme.SpamBlockerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: SpamBlockerViewModel by viewModels {
        SpamBlockerViewModel.Factory(SpamBlockerApp.instance.spamRepository)
    }

    private val billingManager by lazy { SpamBlockerApp.instance.billingManager }

    private val roleRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            checkAndApplyRoleStatus()
        } else {
            checkAndApplyRoleStatus()
        }
    }

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            lifecycleScope.launch {
                try {
                    val json = viewModel.exportRulesJson()
                    contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray())
                    }
                    Toast.makeText(this@MainActivity, "Rules backup exported successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Failed to export rules: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            lifecycleScope.launch {
                try {
                    val json = contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader().readText()
                    } ?: ""
                    viewModel.importRulesJson(json) { count ->
                        Toast.makeText(this@MainActivity, "Successfully imported $count rules!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Failed to import rules: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestRequiredPermissions()

        setContent {
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

            SpamBlockerTheme {
                SpamBlockerHomeScreen(
                    uiState = uiState,
                    onRequestRole = { requestCallScreeningRole() },
                    onCompleteOnboarding = { firstPattern, countryCode -> viewModel.completeOnboarding(firstPattern, countryCode) },
                    onReopenOnboarding = { viewModel.reopenOnboarding() },
                    onOpenPaywall = { viewModel.openPaywall() },
                    onClosePaywall = { viewModel.closePaywall() },
                    onStartSubscriptionFlow = { isYearly ->
                        val productId = if (isYearly) BillingManager.PRODUCT_YEARLY else BillingManager.PRODUCT_MONTHLY
                        val subInfo = billingManager.availableSubscriptions.value.firstOrNull { it.productId == productId }
                        if (subInfo?.productDetails != null) {
                            billingManager.launchSubscriptionFlow(this@MainActivity, subInfo.productDetails)
                        } else {
                            Toast.makeText(this@MainActivity, "Connecting to Google Play Store...", Toast.LENGTH_SHORT).show()
                            billingManager.queryProductDetails()
                        }
                    },
                    onSelectTab = { viewModel.selectTab(it) },
                    onSetRuleFilter = { viewModel.setRuleFilter(it) },
                    onToggleEmergencyDisable = { viewModel.toggleEmergencyDisable() },
                    onToggleAllowContacts = { viewModel.toggleAllowContacts() },
                    onSelectLanguage = { langCode -> viewModel.setAppLanguage(langCode) },
                    onExportRules = { exportLauncher.launch("CleanRing_Rules_Backup.json") },
                    onImportRules = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                    onOpenAddDialog = { pattern, type -> viewModel.openAddDialog(pattern, type) },
                    onOpenEditDialog = { rule -> viewModel.openEditDialog(rule) },
                    onCloseAddEditDialog = { viewModel.closeAddEditDialog() },
                    onAddOrUpdateRule = { pattern, type, action, label, countryCode ->
                        viewModel.addOrUpdateRule(pattern, type, action, label, countryCode)
                    },
                    onToggleRuleStatus = { id, isEnabled -> viewModel.toggleRuleStatus(id, isEnabled) },
                    onDeleteRule = { rule -> viewModel.deleteRule(rule) },
                    onClearCallLogs = { viewModel.clearCallLogs() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndApplyRoleStatus()
        billingManager.queryActivePurchases()
    }

    private fun checkAndApplyRoleStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(ROLE_SERVICE) as? RoleManager
            val isRoleHeld = roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
            viewModel.updateRoleStatus(isRoleHeld)

            if (isRoleHeld) {
                try {
                    ProtectionService.startService(this)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to start ProtectionService", e)
                }
            }
        } else {
            viewModel.updateRoleStatus(true)
        }
    }

    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(ROLE_SERVICE) as? RoleManager ?: return
            if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                roleRequestLauncher.launch(intent)
            } else {
                viewModel.updateRoleStatus(true)
                ProtectionService.startService(this)
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CALL_LOG)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
