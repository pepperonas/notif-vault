package io.celox.notifvault

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.celox.notifvault.data.SettingsStore
import io.celox.notifvault.ui.ConversationScreen
import io.celox.notifvault.ui.HomeScreen
import io.celox.notifvault.ui.OnboardingScreen
import io.celox.notifvault.ui.SettingsScreen
import io.celox.notifvault.ui.VaultViewModel
import io.celox.notifvault.ui.theme.NotifVaultTheme
import io.celox.notifvault.util.PermissionUtils
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Uses FragmentActivity so BiometricPrompt works.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotifVaultTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val vm: VaultViewModel = viewModel()
                    val biometricOn by vm.settings.biometricLock.collectAsStateWithLifecycle(initialValue = false)
                    // rememberSaveable so a config change (rotation) doesn't re-lock the app.
                    var unlocked by rememberSaveable { mutableStateOf(false) }

                    if (biometricOn && !unlocked) {
                        LockScreen(onAuthenticate = { promptBiometric { unlocked = true } })
                    } else {
                        AppNav(vm)
                    }
                }
            }
        }
    }

    private fun promptBiometric(onSuccess: () -> Unit) {
        val canAuth = BiometricManager.from(this)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) { onSuccess(); return }

        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
            })
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("NotifVault entsperren")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
        )
    }
}

@Composable
private fun LockScreen(onAuthenticate: () -> Unit) {
    // Prompt once on entry; the button lets the user retry after a cancel/failure
    // instead of being stuck on this screen forever.
    LaunchedEffect(Unit) { onAuthenticate() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("🔒 NotifVault ist gesperrt", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onAuthenticate) { Text("Entsperren") }
        }
    }
}

@Composable
private fun AppNav(vm: VaultViewModel) {
    val nav = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current

    val start = if (PermissionUtils.hasNotificationAccess(context)) "home" else "onboarding"

    NavHost(navController = nav, startDestination = start) {
        composable("onboarding") {
            OnboardingScreen(
                hasAccess = PermissionUtils.hasNotificationAccess(context),
                batteryOptimized = PermissionUtils.isIgnoringBatteryOptimizations(context),
                onGrantAccess = { PermissionUtils.openNotificationAccessSettings(context) },
                onBattery = { PermissionUtils.requestIgnoreBatteryOptimizations(context) },
                onContinue = { nav.navigate("home") { popUpTo("onboarding") { inclusive = true } } }
            )
        }
        composable("home") {
            HomeScreen(
                vm = vm,
                onOpenConversation = { conv, pkg ->
                    nav.navigate("chat/${enc(conv)}/${enc(pkg)}")
                },
                onOpenSettings = { nav.navigate("settings") }
            )
        }
        composable("chat/{conv}/{pkg}") { entry ->
            ConversationScreen(
                vm = vm,
                conversation = dec(entry.arguments?.getString("conv")),
                pkg = dec(entry.arguments?.getString("pkg")),
                onBack = { nav.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(vm = vm, onBack = { nav.popBackStack() })
        }
    }
}

private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
private fun dec(s: String?) = URLDecoder.decode(s ?: "", "UTF-8")
