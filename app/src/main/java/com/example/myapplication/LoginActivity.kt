package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.myapplication.data.firebase.AuthRepository
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.util.AppLogger
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * Launcher activity that handles user authentication.
 * Provides two sign-in methods:
 *   1. Email / Password (sign-in + sign-up)
 *   2. Google Sign-In via the Credential Manager API
 *
 * If a user is already authenticated the activity immediately
 * forwards to [MainActivity].
 */
class LoginActivity : ComponentActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Auto-login: skip if session already exists ──────────────
        if (authRepository.isLoggedIn) {
            navigateToMain()
            return
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        authRepository = authRepository,
                        onAuthSuccess = { navigateToMain() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    /** Navigate to MainActivity and remove LoginActivity from the back stack. */
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "LoginActivity"

        const val WEB_CLIENT_ID = "1083062915592-fhd4p57a3rvfs9e3610tl32go8t1q5uu.apps.googleusercontent.com"
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Composable UI
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun LoginScreen(
    authRepository: AuthRepository,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── Form state ──────────────────────────────────────────────────
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var isSignUpMode by rememberSaveable { mutableStateOf(false) }

    // ── Validation helpers ──────────────────────────────────────────
    val emailError = when {
        email.isBlank() -> null                       // don't show error while empty
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
        else -> null
    }
    val passwordError = when {
        password.isBlank() -> null
        password.length < 6 -> "Password must be at least 6 characters"
        else -> null
    }
    val formValid = email.isNotBlank() && password.isNotBlank()
            && emailError == null && passwordError == null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Title ───────────────────────────────────────────────────
        Text(
            text = if (isSignUpMode) "Create Account" else "Welcome Back",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isSignUpMode) "Sign up with your email" else "Sign in to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Email field ─────────────────────────────────────────────
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            label = { Text("Email") },
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Password field ──────────────────────────────────────────
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Sign-In / Sign-Up button ────────────────────────────────
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        if (isSignUpMode) {
                            authRepository.signUpWithEmail(email, password)
                            Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show()
                        } else {
                            authRepository.signInWithEmail(email, password)
                        }
                        onAuthSuccess()
                    } catch (e: Exception) {
                        AppLogger.e("LoginScreen", "Email auth failed", e)
                        Toast.makeText(
                            context,
                            e.localizedMessage ?: "Authentication failed",
                            Toast.LENGTH_LONG
                        ).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = formValid && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isSignUpMode) "Sign Up" else "Sign In")
        }

        // ── Toggle sign-in / sign-up ────────────────────────────────
        TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
            Text(
                if (isSignUpMode) "Already have an account? Sign In"
                else "Don't have an account? Sign Up"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Divider ─────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "  OR  ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Google Sign-In button ───────────────────────────────────
        GoogleSignInButton(
            authRepository = authRepository,
            onAuthSuccess = onAuthSuccess,
            setLoading = { isLoading = it },
            isLoading = isLoading
        )
    }
}

/**
 * Google Sign-In button that uses the Credential Manager API.
 * On click it launches the system credential picker, retrieves
 * the Google ID token and exchanges it for a Firebase session.
 */
@Composable
private fun GoogleSignInButton(
    authRepository: AuthRepository,
    onAuthSuccess: () -> Unit,
    setLoading: (Boolean) -> Unit,
    isLoading: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)

    OutlinedButton(
        onClick = {
            scope.launch {
                setLoading(true)
                try {
                    // Build the Google ID request for Credential Manager
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)   // show all accounts
                        .setServerClientId(LoginActivity.WEB_CLIENT_ID)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    // Launch the system credential picker
                    val result = credentialManager.getCredential(
                        context = context as ComponentActivity,
                        request = request
                    )

                    // Extract Google ID token from the credential
                    val googleIdToken = GoogleIdTokenCredential
                        .createFrom(result.credential.data)
                        .idToken

                    // Exchange the token for a Firebase session
                    authRepository.signInWithGoogle(googleIdToken)
                    onAuthSuccess()

                } catch (e: GetCredentialCancellationException) {
                    // User dismissed the picker – do nothing
                    AppLogger.i("GoogleSignIn", "User cancelled Google Sign-In")
                } catch (e: NoCredentialException) {
                    Toast.makeText(
                        context,
                        "No Google accounts found on this device",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    AppLogger.e("GoogleSignIn", "Google Sign-In failed", e)
                    Toast.makeText(
                        context,
                        e.localizedMessage ?: "Google Sign-In failed",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    setLoading(false)
                }
            }
        },
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_google),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Continue with Google")
    }
}
