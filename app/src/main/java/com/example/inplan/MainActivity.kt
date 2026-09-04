package com.example.inplan

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.inplan.data.repository.AuthRepository
import com.example.inplan.ui.screens.Routes
import com.example.inplan.ui.screens.SplashScreen
import com.example.inplan.ui.screens.TripSplitNavHost
import com.example.inplan.ui.theme.BgBase
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Splash is a COLD-START-ONLY artifact — exactly the WhatsApp/Zomato
 * pattern:
 *  - Fresh install -> onCreate runs -> Splash while we resolve where to
 *    start -> Auth.
 *  - Signed in, app backgrounded then reopened (Activity never
 *    destroyed) -> onCreate does NOT run again -> Splash never shows.
 *  - Activity/process killed, then reopened -> onCreate runs again
 *    (fresh instance) -> Splash shows again while we re-resolve.
 *  - Logging out or back in while the app stays alive -> handled
 *    entirely inside TripSplitNavHost's own NavController (plain
 *    instant navigation) -> no Splash, because it's not a cold start.
 *
 * There is deliberately no reactive "show splash on every auth change"
 * layer here. startDestination is resolved exactly ONCE per onCreate.
 */
private const val MIN_SPLASH_MILLIS = 2000L

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var supabaseClient: SupabaseClient
    @Inject lateinit var authRepository: AuthRepository

    private var pendingDeepLinkTripId by mutableStateOf<String?>(null)

    private var startDestination by mutableStateOf<String?>(null)
    private var pendingJoinTripIdForAuth by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // draws behind status/nav bars, makes them transparent
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        val deepLinkTripId = intent?.data?.getQueryParameter("tripId")
        pendingDeepLinkTripId = deepLinkTripId

        lifecycleScope.launch {
            val startedAt = System.currentTimeMillis()

            supabaseClient.auth.awaitInitialization()
            val isSignedIn = supabaseClient.auth.currentUserOrNull() != null

            val needsProfileSetup = isSignedIn &&
                    !runCatching { authRepository.hasUpiId() }.getOrDefault(true)

            val destination = when {
                !isSignedIn -> Routes.AUTH
                needsProfileSetup -> Routes.PROFILE
                deepLinkTripId != null -> Routes.joinTrip(deepLinkTripId)
                else -> Routes.TRIP_LIST
            }

            // Force a real suspension point so Splash always gets at
            // least one frame to actually paint, and so its duration is
            // consistent instead of depending on how fast
            // awaitInitialization()/hasUpiId() happen to resolve.
            //
            // Without this: if awaitInitialization() doesn't genuinely
            // suspend (common once the SDK is already initialized/cached
            // — Deferred.await() on an already-completed Deferred often
            // returns synchronously), this whole coroutine can run to
            // completion BEFORE setContent() below is even called —
            // meaning `dest` is already non-null on the very first
            // composition and SplashScreen() never composes at all.
            val elapsed = System.currentTimeMillis() - startedAt
            if (elapsed < MIN_SPLASH_MILLIS) {
                delay(MIN_SPLASH_MILLIS - elapsed)
            }

            pendingJoinTripIdForAuth = if (!isSignedIn || needsProfileSetup) deepLinkTripId else null
            startDestination = destination
        }

        window.navigationBarColor = BgBase.toArgb()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val dest = startDestination
                    if (dest == null) {
                        SplashScreen()
                    } else {
                        TripSplitNavHost(
                            startDestination = dest,
                            pendingJoinTripId = pendingJoinTripIdForAuth,
                            liveDeepLinkTripId = pendingDeepLinkTripId
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val deepLinkTripId = intent.data?.getQueryParameter("tripId")
        if (deepLinkTripId != null) {
            pendingDeepLinkTripId = deepLinkTripId
        }
    }
}