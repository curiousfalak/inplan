package com.example.inplan.ui.screens.trip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.inplan.R
import com.example.inplan.data.model.Trip
import com.example.inplan.ui.screens.Routes
import com.example.inplan.ui.theme.*
import com.example.inplan.viewmodel.TripViewModel
import kotlin.math.abs
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale


// --- Ticket / backdrop accent colors ----------------------------------------
// Kept in the theme's existing palette family (soft blue-greys + the single
// amber accent used for "ACTIVE") rather than the darker navy / saturated
// orange that had drifted in — those read as a different app entirely next
// to InPlanBlue and BgBase.
private val TicketPaper = Color(0xFFFFFFFF)
private val TicketBorder = Color(0xFFE6E9F5)
private val PerforationLine = Color(0xFFD9DDEE)
private val StampAmber = Color(0xFFF0A23A)
private val StampAmberDeep = Color(0xFFB9701E)

// --- Monogram fallback palette ----------------------------------------------
// Used when a trip name doesn't match any known category icon below. Picked
// deterministically from the trip name's hash so the same trip always gets
// the same color across recompositions/sessions.
private val MonogramPalette = listOf(
    InPlanBlue, StampAmberDeep, GetsBackGreen, Color(0xFF7C5CFA), Color(0xFFE0567A)
)
private fun monogramColor(name: String): Color =
    MonogramPalette[abs(name.hashCode()) % MonogramPalette.size]

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    onOpenTrip: (String) -> Unit,
    onOpenProfile: () -> Unit = {},
    vm: TripViewModel = hiltViewModel()
) {
    val trips by vm.trips.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var newTripName by remember { mutableStateOf("") }

    var showJoinDialog by remember { mutableStateOf(false) }
    var joinCode by remember { mutableStateOf("") }
    var joinError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { vm.watchTrips() }

    // The dashed flight path needs to sit behind BOTH the TopAppBar and the
    // list content, so it's drawn on its own layer underneath a
    // fully-transparent Scaffold rather than inside any one slot.
    //
    // NOTE: bottomBar was removed from this screen's own Scaffold — the
    // pill bottom bar now lives once, at the nav-host level in
    // TripSplitNavHost, so it persists across screens instead of only
    // showing here. This Scaffold is kept for the topBar + FAB only.
    Box(modifier = Modifier.fillMaxSize().background(BgBase)) {

        FlightPathBackdrop(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp)
                .height(130.dp) // was 220.dp — shortened so the curve/dot
                // resolve before the first card instead of
                // trailing underneath it
                .align(Alignment.TopStart)
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                // Custom header instead of TopAppBar's title/actions slots.
                // TopAppBar vertically centers "actions" against the FULL
                // bar height, which is why "+ Join" was floating mid-way
                // down when the title had two lines. Building this as a
                // plain Column with its own Row gives exact control: the
                // logo/wordmark and "+ Join" sit in the SAME Row, so they
                // share a baseline no matter how tall "My Trips" is below.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()

                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Restored: the pasted version had this swapped
                            // for a bare Spacer, dropping the logo mark.


                            Text(
                                "InPlan",
                                fontFamily = FontFamily.Cursive,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                                color = InPlanBlue
                            )
                        }
                        val joinPulse = rememberInfiniteTransition(label = "joinPulse")
                        val joinScale by joinPulse.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.08f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(700, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "joinScale"
                        )
                        val joinGlowAlpha by joinPulse.animateFloat(
                            initialValue = 0.10f,
                            targetValue = 0.28f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(700, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "joinGlowAlpha"
                        )

                        Box(
                            modifier = Modifier
                                .scale(joinScale)
                                .clip(RoundedCornerShape(50))
                                .background(InPlanBlue.copy(alpha = joinGlowAlpha))
                        ) {
                            TextButton(onClick = {
                                joinError = null
                                joinCode = ""
                                showJoinDialog = true
                            }) {
                                Text("+ Join", color = InPlanBlue, fontWeight = FontWeight.Bold)
                            }
                        } // keep both.
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "My Trips",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = TextPrimary
                    )
                }
            },
            floatingActionButton = {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(listOf(InPlanBlueLight, InPlanBlue))
                        )
                        .clickable { showDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New trip", tint = InPlanWhite)
                }
            }
        ) { padding ->
            if (trips.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No trips yet.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tap + to start one, or Join with a code from a friend.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 28.dp, // was 140.dp — that was the big empty
                        // gap above the first trip card
                        bottom = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(trips) { trip: Trip ->
                        TripRow(trip = trip, onClick = { onOpenTrip(trip.id) })
                    }
                    item { Spacer(Modifier.height(72.dp)) } // room for FAB
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = CardSurface,
            title = { Text("New trip", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTripName,
                    onValueChange = { newTripName = it },
                    label = { Text("Trip name, e.g. Hostel to Home Aug") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InPlanBlue,
                        cursorColor = InPlanBlue
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTripName.isNotBlank()) {
                        vm.createTrip(newTripName) { onOpenTrip(it.id) }
                        newTripName = ""
                        showDialog = false
                    }
                }) { Text("Create", color = InPlanBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            containerColor = CardSurface,
            title = { Text("Join a trip", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = joinCode,
                        onValueChange = {
                            joinCode = it.uppercase()
                            joinError = null
                        },
                        label = { Text("Enter invite code, e.g. K7X9PQ") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InPlanBlue,
                            cursorColor = InPlanBlue
                        )
                    )
                    if (joinError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(joinError!!, color = ErrorRed, fontSize = 12.5.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (joinCode.isBlank()) {
                        joinError = "Please enter a code"
                    } else {
                        vm.joinTripByCode(joinCode) { trip ->
                            joinCode = ""
                            showJoinDialog = false
                            vm.loadTrips() // trip_members changed, not trips — realtime won't catch this
                            onOpenTrip(trip.id)
                        }
                    }
                }) { Text("Join", color = InPlanBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

/**
 * Floating, rounded "pill" bottom navigation bar for InPlan's top-level tabs
 * — styled after a reference app screenshot (rounded floating bar with a
 * soft highlight behind the selected tab), instead of a flush-to-edge
 * Material3 NavigationBar. Lives here as a reusable composable but is now
 * hosted once at the nav-graph level (TripSplitNavHost) rather than inside
 * this screen, so it persists across every destination instead of only
 * showing on the trip list.
 */
@Composable
fun InPlanBottomBar(
    currentRoute: String,
    onTripsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = InPlanBlue.copy(alpha = 0.25f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(CardSurface)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PillNavItem(
                modifier = Modifier.weight(1f),
                selected = currentRoute == Routes.TRIP_LIST,
                icon = Icons.Default.Home,
                label = "Trips",
                onClick = onTripsClick
            )
            PillNavItem(
                modifier = Modifier.weight(1f),
                selected = currentRoute == Routes.PROFILE,
                icon = Icons.Default.Person,
                label = "Profile",
                onClick = onProfileClick
            )
        }
    }
}

/**
 * One tab inside the floating pill bar. Selected state gets a soft rounded
 * highlight behind the icon+label, matching the "Home" tab in the reference
 * screenshot.
 */
@Composable
private fun PillNavItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val contentColor = if (selected) InPlanBlue else TextSecondary

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .then(
                if (selected)
                    Modifier.background(InPlanBlue.copy(alpha = 0.12f))
                else Modifier
            )
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * The faint dashed flight-path curve with a single waypoint dot, drawn behind
 * the header and top of the trip list — mirrors the dotted wavy line from the
 * design mockup. Sized as a fraction of the available width/height so it
 * scales across devices instead of using fixed pixel coordinates.
 */
@Composable
private fun FlightPathBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(-20f, h * 0.28f)
            cubicTo(
                w * 0.28f, h * 0.05f,
                w * 0.42f, h * 0.55f,
                w * 0.66f, h * 0.38f
            )
            cubicTo(
                w * 0.82f, h * 0.26f,
                w * 1.02f, h * 0.05f,
                w * 1.15f, h * 0.42f
            )
        }

        drawPath(
            path = path,
            color = PerforationLine,
            style = Stroke(
                width = 1.6.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 9f), 0f)
            )
        )

        // Waypoint dot roughly where the curve crosses the trip card's row.
        drawCircle(
            color = StampAmber,
            radius = 3.5.dp.toPx(),
            center = Offset(w * 0.66f, h * 0.38f)
        )
    }
}

/**
 * Resolves a trip name to a known category icon (Home, beach, mountain,
 * food, etc). Returns null when nothing matches confidently, so the caller
 * can fall back to a letter monogram instead of a wrong/generic icon.
 *
 * This intentionally does NOT try to cover every possible destination —
 * that's an unbounded set. It only needs to catch the common cases; the
 * monogram fallback handles the long tail so every trip still gets a
 * distinct, non-childish avatar.
 */
private fun getTripIcon(name: String): ImageVector? {
    val trip = name.lowercase()
    return when {
        trip.contains("home") -> Icons.Default.Home
        trip.contains("beach") || trip.contains("goa") -> Icons.Default.BeachAccess
        trip.contains("mountain") || trip.contains("hill") -> Icons.Default.Terrain
        trip.contains("trek") || trip.contains("hike") -> Icons.Default.Hiking
        trip.contains("food") -> Icons.Default.Restaurant
        trip.contains("road") -> Icons.Default.DirectionsCar
        trip.contains("flight") -> Icons.Default.Flight
        trip.contains("party") || trip.contains("wedding") -> Icons.Default.Celebration
        else -> null
    }
}

/**
 * Boarding-pass styled trip card: an icon stub on the left separated by a
 * dashed perforation, with two "punched" notches on the outer edges (faked
 * by drawing small circles in the screen's background color on top of the
 * card). The status shows as a tilted stamp rather than a flat pill.
 */
@Composable
private fun TripRow(trip: Trip, onClick: () -> Unit) {
    val isActive = trip.status.equals("active", ignoreCase = true)
    val isSettled = trip.status.equals("settled", ignoreCase = true)

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = InPlanBlue.copy(alpha = 0.18f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(TicketPaper)
                .border(1.dp, TicketBorder, RoundedCornerShape(20.dp))
                .clickable { onClick() }
        ) {
            // --- Stub: icon tab + dashed perforation on its right edge ---
            Box(
                modifier = Modifier
                    .width(88.dp)
                    .fillMaxHeight()
                    .drawBehindDashedEdge(PerforationLine),
                contentAlignment = Alignment.Center
            ) {
                val icon = getTripIcon(trip.name)
                val bgBrush = if (icon != null) {
                    // Known category — keep the standard brand gradient.
                    Brush.linearGradient(listOf(InPlanBlueLight, InPlanBlue))
                } else {
                    // Unrecognized destination — deterministic per-trip
                    // accent so the monogram cards stay visually distinct
                    // from each other in a long list.
                    val base = monogramColor(trip.name)
                    Brush.linearGradient(listOf(base.copy(alpha = 0.85f), base))
                }

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bgBrush),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = InPlanWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = trip.name.trim().firstOrNull()?.uppercase() ?: "T",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = InPlanWhite
                        )
                    }
                }
            }

            // --- Body: destination name, arrow, status stamp, "Trip" label ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trip.name,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(BgBase),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isActive || isSettled) {
                        StatusStamp(
                            text = if (isSettled) "SETTLED" else "ACTIVE",
                            color = if (isSettled) GetsBackGreen else StampAmber,
                            colorDeep = if (isSettled) GetsBackGreen else StampAmberDeep
                        )
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Text(
                            trip.status.replaceFirstChar { it.uppercase() },
                            color = TextSecondary,
                            fontSize = 12.5.sp
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    Text(
                        text = "Trip",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // --- Punched notches on the outer edges of the ticket ---
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(BgBase)
                .align(Alignment.CenterStart)
                .offset(x = (-10).dp)
        )
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(BgBase)
                .align(Alignment.CenterEnd)
                .offset(x = 10.dp)
        )
    }
}

/** Small tilted "ink stamp" badge used for trip status (Active / Settled). */
@Composable
private fun StatusStamp(text: String, color: Color, colorDeep: Color) {
    Box(
        modifier = Modifier
            .graphicsLayer { rotationZ = -3f }
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.08f))
            .border(1.3.dp, color, RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.5.sp,
            color = colorDeep
        )
    }
}

/**
 * Draws a dashed vertical line along the right edge of the modified element —
 * the perforation between the icon stub and the ticket body.
 */
private fun Modifier.drawBehindDashedEdge(lineColor: Color): Modifier = this
    .padding(vertical = 20.dp)
    .drawWithContent {
        drawContent()
        drawLine(
            color = lineColor,
            start = Offset(size.width, 0f),
            end = Offset(size.width, size.height),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
        )
    }