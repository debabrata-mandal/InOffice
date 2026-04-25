package com.inoffice.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.common.api.ApiException
import com.inoffice.app.core.sync.DriveSyncCoordinator
import com.inoffice.app.navigation.InOfficeNavHost
import com.inoffice.app.feature.auth.AuthGateRoute
import com.inoffice.app.ui.theme.InOfficeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var driveSyncCoordinator: DriveSyncCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InOfficeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    val driveScope = remember { Scope("https://www.googleapis.com/auth/drive.appdata") }
                    val signInClient =
                        remember(context) {
                            val options =
                                GoogleSignInOptions
                                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .requestScopes(driveScope)
                                    .build()
                            GoogleSignIn.getClient(context, options)
                        }

                    var authChecked by remember { mutableStateOf(false) }
                    var isSignedIn by remember { mutableStateOf(false) }
                    var signedInEmail by remember { mutableStateOf<String?>(null) }
                    var isSigningIn by remember { mutableStateOf(false) }
                    var authErrorMessage by remember { mutableStateOf<String?>(null) }

                    val signInLauncher =
                        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                            isSigningIn = false
                            val account =
                                try {
                                    GoogleSignIn
                                        .getSignedInAccountFromIntent(result.data)
                                        .getResult(ApiException::class.java)
                                } catch (error: ApiException) {
                                    authErrorMessage = getString(R.string.auth_error_generic, error.statusCode)
                                    null
                                }
                            val resolvedAccount = account ?: GoogleSignIn.getLastSignedInAccount(context)
                            isSignedIn = resolvedAccount != null
                            signedInEmail = resolvedAccount?.email
                            if (isSignedIn) {
                                authErrorMessage = null
                            } else if (authErrorMessage == null) {
                                authErrorMessage = getString(R.string.auth_error_no_account)
                            }
                            authChecked = true
                        }

                    LaunchedEffect(Unit) {
                        val account = GoogleSignIn.getLastSignedInAccount(context)
                        isSignedIn = account != null
                        signedInEmail = account?.email
                        authChecked = true
                    }

                    LaunchedEffect(isSignedIn) {
                        if (isSignedIn) {
                            runCatching { driveSyncCoordinator.runStartupSyncIfNeeded() }
                        }
                    }

                    if (authChecked && isSignedIn) {
                        val navController = rememberNavController()
                        InOfficeNavHost(
                            navController = navController,
                            signedInEmail = signedInEmail,
                            onSignOut = {
                                signInClient
                                    .signOut()
                                    .addOnCompleteListener {
                                        signedInEmail = null
                                        isSignedIn = false
                                        authChecked = true
                                        isSigningIn = false
                                        authErrorMessage = null
                                    }
                            },
                        )
                    } else {
                        AuthGateRoute(
                            isLoading = !authChecked || isSigningIn,
                            errorMessage = authErrorMessage,
                            onSignIn = {
                                authErrorMessage = null
                                isSigningIn = true
                                signInLauncher.launch(signInClient.signInIntent)
                            },
                        )
                    }
                }
            }
        }
    }
}
