package com.example.inplan.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.inplan.ui.screens.auth.AuthScreen
import com.example.inplan.ui.screens.auth.ProfileScreen
import com.example.inplan.ui.screens.expense.TripDetailScreen
import com.example.inplan.ui.screens.trip.InPlanBottomBar
import com.example.inplan.ui.screens.trip.JoinTripScreen
import com.example.inplan.ui.screens.trip.TripListScreen

object Routes {
    const val AUTH = "auth"
    const val TRIP_LIST = "trip_list"
    const val TRIP_DETAIL = "trip_detail/{tripId}"
    const val JOIN_TRIP = "join_trip/{tripId}"
    const val PROFILE = "profile"

    fun tripDetail(tripId: String) = "trip_detail/$tripId"
    fun joinTrip(tripId: String) = "join_trip/$tripId"

    // Routes whose destination.route (the pattern, e.g. "trip_detail/{tripId}")
    // should show the bottom bar. Auth and the join-trip deep-link flow are
    // deliberately excluded — you're not "in the app" yet on those screens.
    val BOTTOM_BAR_ROUTES = setOf(TRIP_LIST, TRIP_DETAIL, PROFILE)
}

/**
 * Owns ALL in-app navigation for the lifetime of a single MainActivity
 * instance, including sign-in and sign-out — both are just
 * navController.navigate() calls within this one NavHost, same as any
 * other screen transition. There is no outer "splash on logout" layer:
 * Splash only ever shows once, before this composable is first mounted
 * (see MainActivity), matching how logout behaves in WhatsApp/Zomato —
 * instant, no reload flash.
 */
@Composable
fun TripSplitNavHost(
    startDestination: String,
    pendingJoinTripId: String? = null,
    liveDeepLinkTripId: String? = null
) {
    val navController: NavHostController = rememberNavController()
    val pendingJoinTripIdState = remember { mutableStateOf(pendingJoinTripId) }

    LaunchedEffect(liveDeepLinkTripId) {
        if (liveDeepLinkTripId != null) {
            navController.navigate(Routes.joinTrip(liveDeepLinkTripId))
        }
    }

    // destination?.route is the route PATTERN (e.g. "trip_detail/{tripId}"),
    // not the resolved path with a real tripId — that's what lets a single
    // check work for both TRIP_LIST/PROFILE (no args) and TRIP_DETAIL (has
    // an arg) without pulling the argument out here.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (currentRoute in Routes.BOTTOM_BAR_ROUTES) {
                InPlanBottomBar(
                    currentRoute = currentRoute ?: Routes.TRIP_LIST,
                    onTripsClick = {
                        navController.navigate(Routes.TRIP_LIST) {
                            popUpTo(Routes.TRIP_LIST) { inclusive = true; saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onProfileClick = {
                        navController.navigate(Routes.PROFILE) {
                            popUpTo(Routes.TRIP_LIST) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.AUTH) {
                AuthScreen(onSignedIn = { needsProfileSetup ->
                    val pendingId = pendingJoinTripIdState.value
                    when {
                        needsProfileSetup -> {
                            navController.navigate(Routes.PROFILE) {
                                popUpTo(Routes.AUTH) { inclusive = true }
                            }
                            pendingJoinTripIdState.value = pendingId
                        }
                        pendingId != null -> {
                            pendingJoinTripIdState.value = null
                            navController.navigate(Routes.joinTrip(pendingId)) {
                                popUpTo(Routes.AUTH) { inclusive = true }
                            }
                        }
                        else -> {
                            navController.navigate(Routes.TRIP_LIST) {
                                popUpTo(Routes.AUTH) { inclusive = true }
                            }
                        }
                    }
                })
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onDone = {
                        val pendingId = pendingJoinTripIdState.value
                        if (pendingId != null) {
                            pendingJoinTripIdState.value = null
                            navController.navigate(Routes.joinTrip(pendingId)) {
                                popUpTo(Routes.PROFILE) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Routes.TRIP_LIST) {
                                popUpTo(Routes.PROFILE) { inclusive = true }
                            }
                        }
                    },
                    onLoggedOut = {
                        // Plain instant navigation back to Auth — no
                        // splash. This is not a cold start, so nothing
                        // else needs to happen here beyond resetting the
                        // back stack.
                        navController.navigate(Routes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.TRIP_LIST) {
                TripListScreen(
                    onOpenTrip = { tripId ->
                        navController.navigate(Routes.tripDetail(tripId))
                    },
                    onOpenProfile = {
                        navController.navigate(Routes.PROFILE)
                    }
                )
            }
            composable(Routes.TRIP_DETAIL) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                TripDetailScreen(tripId = tripId)
            }
            composable(
                route = Routes.JOIN_TRIP,
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                JoinTripScreen(
                    tripId = tripId,
                    onJoined = {
                        navController.navigate(Routes.tripDetail(tripId)) {
                            popUpTo(Routes.JOIN_TRIP) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}