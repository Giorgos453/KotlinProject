package com.example.myapplication.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Repository that encapsulates all Firebase Authentication logic.
 * Activities/ViewModels call these suspend functions instead of using FirebaseAuth directly.
 */
class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /** Currently signed-in user, or null if not authenticated. */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /** Convenience check – true when a user session exists. */
    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    // ── Email / Password ────────────────────────────────────────────────

    /**
     * Creates a new account with [email] and [password].
     * @return the [FirebaseUser] on success.
     * @throws Exception with a user-facing message on failure
     *         (e.g. weak password, email already in use).
     */
    suspend fun signUpWithEmail(email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("Registration succeeded but user is null")
    }

    /**
     * Signs in an existing user with [email] and [password].
     * @return the [FirebaseUser] on success.
     * @throws Exception on invalid credentials or network errors.
     */
    suspend fun signInWithEmail(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("Sign-in succeeded but user is null")
    }

    // ── Google Sign-In ──────────────────────────────────────────────────

    /**
     * Exchanges a Google ID [idToken] (obtained via Credential Manager)
     * for a Firebase session.
     * @return the [FirebaseUser] on success.
     */
    suspend fun signInWithGoogle(idToken: String): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user ?: throw Exception("Google sign-in succeeded but user is null")
    }

    // ── Sign-out ────────────────────────────────────────────────────────

    /** Signs the current user out of Firebase. */
    fun signOut() {
        auth.signOut()
    }
}
