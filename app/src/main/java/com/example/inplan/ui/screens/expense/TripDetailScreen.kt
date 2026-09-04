package com.example.inplan.ui.screens.expense

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.inplan.data.model.Expense
import com.example.inplan.data.model.PollWithVotes
import com.example.inplan.data.model.Profile
import com.example.inplan.data.model.TripBalance
import com.example.inplan.ui.theme.BgBase
import com.example.inplan.ui.theme.CancelledOrange
import com.example.inplan.ui.theme.CardBorder
import com.example.inplan.ui.theme.CardSurface
import com.example.inplan.ui.theme.ConvertedAmber
import com.example.inplan.ui.theme.ErrorRed
import com.example.inplan.ui.theme.GetsBackGreen
import com.example.inplan.ui.theme.InPlanBlue
import com.example.inplan.ui.theme.InPlanBlueLight
import com.example.inplan.ui.theme.InPlanWhite
import com.example.inplan.ui.theme.OwesRed
import com.example.inplan.ui.theme.TextPrimary
import com.example.inplan.ui.theme.TextSecondary
import com.example.inplan.viewmodel.TripViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// --- Design tokens -----------------------------------------------------------
// A slightly more refined accent set layered on top of the existing theme
// colors. Kept local to this screen so nothing else has to change; promote
// to ui.theme later if the same language gets reused elsewhere.
private val AmberAccent = Color(0xFFF0A23A)
private val AmberDeep = Color(0xFFB9701E)
private val AmberTint = Color(0xFFFCEEDC)
private val BlueTint = Color(0xFFE9EDFC)
private val ShadowSoft = Color(0xFF17224D)
private val GlassStroke = Color(0x33FFFFFF)
private val HairlineDivider = Color(0xFFEDEFF7)

private val CardShape = RoundedCornerShape(20.dp)
private val SheetFieldShape = RoundedCornerShape(16.dp)

// Height reserved at the bottom of the list so the last cards always clear
// the floating FAB instead of scrolling underneath it. 60dp FAB + the
// Scaffold's own ~16dp margin + a bit of breathing room.
private val FabClearance = 100.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(tripId: String, vm: TripViewModel = hiltViewModel()) {
    val expenses by vm.expenses.collectAsState()
    val balances by vm.balances.collectAsState()
    val members by vm.tripMembers.collectAsState()
    val trip by vm.currentTrip.collectAsState()
    val polls by vm.polls.collectAsState()
    val profileNames by vm.profileNames.collectAsState()
    val balanceProfiles by vm.balanceProfiles.collectAsState()
    var showAddExpense by remember { mutableStateOf(false) }
    var showCreatePoll by remember { mutableStateOf(false) }
    var convertingPoll by remember { mutableStateOf<PollWithVotes?>(null) }
    var tab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    // Drives the FAB's own show/hide animation (see fabVisible below) —
    // kept separate from list scroll state so switching tabs doesn't fight
    // with it.
    val listState = rememberLazyListState()

    // Scroll-direction tracking for the FAB: visible while scrolling up or
    // at rest, hidden while actively scrolling down. This keeps a long
    // "Live feed" / "Who owes" / "Polls" list feeling uncluttered instead
    // of permanently having a button parked over the content — it only
    // shows up when the person is likely to want it (idle, or scrolling
    // back toward the top).
    var previousScrollIndex by remember { mutableStateOf(0) }
    var previousScrollOffset by remember { mutableStateOf(0) }
    val fabVisible by remember {
        derivedStateOf {
            val visible = if (previousScrollIndex != listState.firstVisibleItemIndex) {
                previousScrollIndex > listState.firstVisibleItemIndex
            } else {
                previousScrollOffset >= listState.firstVisibleItemScrollOffset
            }
            previousScrollIndex = listState.firstVisibleItemIndex
            previousScrollOffset = listState.firstVisibleItemScrollOffset
            visible
        }
    }

    val errorMessage by vm.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(message = it, withDismissAction = true, duration = SnackbarDuration.Long)
            vm.clearError()
        }
    }

    LaunchedEffect(tripId) {
        vm.watchTrip(tripId)
        vm.watchExpenses(tripId)
        vm.loadBalances(tripId)
        vm.watchTripMembers(tripId)
        vm.loadTrip(tripId)
        vm.watchPolls(tripId)
    }

    LaunchedEffect(balances) {
        if (balances.isNotEmpty()) {
            vm.loadBalanceProfiles(balances.map { it.userId })
        }
    }

    LaunchedEffect(tripId, tab) {
        if (tab == 2) {
            while (true) {
                kotlinx.coroutines.delay(4000)
                vm.refreshPolls(tripId)
            }
        }
    }

    LaunchedEffect(tripId, tab) {
        if (tab == 1) {
            while (true) {
                kotlinx.coroutines.delay(4000)
                vm.loadBalances(tripId)
            }
        }
    }

    LaunchedEffect(convertingPoll) {
        convertingPoll?.let { pv ->
            val inUserIds = pv.votes.filter { it.isIn }.map { it.userId }
            if (inUserIds.isNotEmpty()) vm.loadProfileNames(inUserIds)
        }
    }

    Scaffold(
        containerColor = BgBase,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // AnimatedVisibility instead of always rendering the FAB —
            // fades/scales it out while the person is scrolling down
            // through a long list, and brings it back on scroll-up or once
            // scrolling settles.
            AnimatedVisibility(
                visible = fabVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                ModernFab(
                    icon = Icons.Default.Add,
                    contentDescription = if (tab == 2) "New poll" else "Add expense",
                    onClick = { if (tab == 2) showCreatePoll = true else showAddExpense = true }
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                // Was 24.dp — too small to clear the FAB, so the last
                // couple of rows in a longer list (e.g. "Meal", "Dal")
                // scrolled in underneath it. FabClearance reserves enough
                // empty space at the bottom that every row stays fully
                // visible and tappable, FAB included.
                contentPadding = PaddingValues(bottom = FabClearance)
            ) {
                item {
                    TripHero(
                        name = trip?.name ?: "Trip",
                        memberCount = members.size,
                        expenseCount = expenses.size,
                        onShare = {
                            val code = trip?.inviteCode.orEmpty()
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    if (code.isNotBlank())
                                        "Join our trip on InPlan! \uD83D\uDCF1 Open the app → tap 'Join' → enter code: $code"
                                    else
                                        "Join our trip on InPlan! Open the app and tap 'Join' (loading code…)"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share trip"))
                        }
                    )
                }

                item {
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        TripTabs(selected = tab, onSelect = { tab = it })
                    }
                }

                item { Spacer(Modifier.height(4.dp)) }

                when (tab) {
                    0 -> expenseFeedItems(expenses)
                    1 -> {
                        balanceListItems(
                            balances = balances,
                            currentUserId = vm.currentUserId,
                            profiles = balanceProfiles,
                            onSettle = { onResult -> vm.settleUp(tripId, onResult) },
                            onShowMessage = { msg ->
                                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        )
                    }
                    else -> pollListItems(
                        polls = polls,
                        currentUserId = vm.currentUserId,
                        onVote = { pollId, isIn -> vm.votePoll(tripId, pollId, isIn) },
                        onLock = { pollId -> vm.lockPoll(tripId, pollId) },
                        onCancel = { pollId -> vm.cancelPoll(tripId, pollId) },
                        onConvertClick = { pv -> convertingPoll = pv }
                    )
                }
            }
        }
    }

    if (showAddExpense) {
        AddExpenseSheet(
            tripMembers = members,
            onDismiss = { showAddExpense = false },
            onAdd = { title, category, amount, tax, splitIds ->
                vm.addExpense(tripId, title, category, amount, tax, splitIds)
                showAddExpense = false
            }
        )
    }

    if (showCreatePoll) {
        CreatePollSheet(
            onDismiss = { showCreatePoll = false },
            onCreate = { title, estimatedAmount ->
                vm.createPoll(tripId, title, estimatedAmount)
                showCreatePoll = false
            }
        )
    }

    convertingPoll?.let { pv ->
        ConvertPollSheet(
            pollWithVotes = pv,
            profileNames = profileNames,
            onDismiss = { convertingPoll = null },
            onConvert = { title, category, finalAmount, paidByUserId ->
                vm.convertPoll(
                    pollId = pv.poll.id,
                    tripId = tripId,
                    title = title,
                    category = category,
                    finalAmount = finalAmount,
                    estimatedAmount = pv.poll.estimatedAmount,
                    paidByUserId = paidByUserId
                )
                convertingPoll = null
            }
        )
    }
}

// --- Hero header ---------------------------------------------------------
// One continuous piece instead of a TopAppBar + a separate "ticket" card
// underneath: the gradient plane carries the title, and two frosted-glass
// stat chips are anchored so they straddle the seam between the gradient
// and the page background — a bit of depth instead of a flat stacked card.

@Composable
private fun TripHero(name: String, memberCount: Int, expenseCount: Int, onShare: () -> Unit) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(196.dp)

                .background(
                    Brush.linearGradient(
                        listOf(InPlanBlueLight, InPlanBlue),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 900f)
                    )
                )
        ) {
            HeaderPathBackdrop(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(top = 74.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding() // ADD THIS instead of just top=18.dp
                    .padding(top = 4.dp, start = 20.dp, end = 12.dp),


                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "TRIP",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InPlanWhite.copy(alpha = .65f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        name,
                        fontFamily = FontFamily.Serif,
                        color = InPlanWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp,
                        lineHeight = 32.sp
                    )
                }
                GlassIconButton(icon = Icons.Default.Share, contentDescription = "Share trip", onClick = onShare)
            }
        }

        // Floating stat strip, pulled up over the gradient's bottom edge.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-26).dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatChip(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Groups,
                label = "MEMBERS",
                value = memberCount.toString()
            )
            StatChip(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Receipt,
                label = "EXPENSES",
                value = expenseCount.toString()
            )
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun StatChip(modifier: Modifier = Modifier, icon: ImageVector, label: String, value: String) {
    Row(
        modifier = modifier
            .shadow(elevation = 10.dp, shape = CardShape, ambientColor = ShadowSoft, spotColor = ShadowSoft)
            .clip(CardShape)
            .background(InPlanWhite)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(BlueTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = InPlanBlue, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = TextPrimary
            )
            Text(
                label,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.5.sp,
                letterSpacing = 1.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun GlassIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(InPlanWhite.copy(alpha = 0.16f))
            .border(1.dp, GlassStroke, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = InPlanWhite, modifier = Modifier.size(18.dp))
    }
}

/**
 * Faint dashed flight-path curve for the header.
 */
@Composable
private fun HeaderPathBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(-10f, h * 0.3f)
            cubicTo(
                w * 0.28f, h * 0.05f,
                w * 0.42f, h * 0.9f,
                w * 0.66f, h * 0.4f
            )
            cubicTo(
                w * 0.82f, h * 0.1f,
                w * 1.02f, h * 0.05f,
                w * 1.15f, h * 0.5f
            )
        }
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.3f),
            style = Stroke(
                width = 1.6.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 9f), 0f)
            )
        )
        drawCircle(
            color = AmberAccent,
            radius = 3.dp.toPx(),
            center = Offset(w * 0.66f, h * 0.4f)
        )
    }
}

/** Maps an expense category to an icon + accent color pair. */
private fun categoryVisuals(category: String?): Triple<ImageVector, Color, Color> =
    when (category?.lowercase()) {
        "food" -> Triple(Icons.Default.Restaurant, AmberTint, AmberDeep)
        "cab" -> Triple(Icons.Default.DirectionsCar, BlueTint, InPlanBlue)
        "stay" -> Triple(Icons.Default.Hotel, BlueTint, InPlanBlue)
        "shopping" -> Triple(Icons.Default.ShoppingBag, AmberTint, AmberDeep)
        else -> Triple(Icons.Default.MoreHoriz, HairlineDivider, TextSecondary)
    }

@Composable
private fun TripTabs(selected: Int, onSelect: (Int) -> Unit) {
    val labels = listOf("Live feed", "Who owes", "Polls")
    val indicatorOffset by animateFloatAsState(
        targetValue = selected.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tabIndicator"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(CardSurface)
            .padding(5.dp)
    ) {
        // Explicit height here breaks the circular constraint that was
        // collapsing the sliding indicator to 0dp: BoxWithConstraints has
        // no size of its own, and a fillMaxHeight() child inside it can't
        // resolve against a parent whose height depends on that same
        // child, so it silently sized to zero. Giving this row a fixed
        // height makes fillMaxHeight() below well-defined.
        BoxWithConstraints(modifier = Modifier.height(42.dp)) {
            val segmentWidth = maxWidth / labels.size
            Box(
                modifier = Modifier
                    .offset(x = segmentWidth * indicatorOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(14.dp), spotColor = InPlanBlue.copy(alpha = 0.5f))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(InPlanBlue, InPlanBlueLight)))
            )
            Row(Modifier.fillMaxSize()) {
                labels.forEachIndexed { index, label ->
                    val isSelected = index == selected
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) InPlanWhite else TextSecondary,
                        label = "tabText"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelect(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = textColor)
                    }
                }
            }
        }
    }
}

// --- Live feed -------------------------------------------------------------

private fun androidx.compose.foundation.lazy.LazyListScope.expenseFeedItems(expenses: List<Expense>) {
    val rupee = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    if (expenses.isEmpty()) {
        item { EmptyState("No expenses yet. Add the first cab or food order.", icon = EmptyIconKind.RECEIPT) }
        return
    }
    items(expenses) { e: Expense ->
        val (icon, iconBg, iconFg) = categoryVisuals(e.category)
        Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 6.dp, shape = CardShape, ambientColor = ShadowSoft, spotColor = ShadowSoft)
                    .clip(CardShape)
                    .background(InPlanWhite)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconFg, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(e.title, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, color = TextPrimary)
                        Spacer(Modifier.height(3.dp))
                        Text(
                            (e.category ?: "other").replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                Text(
                    rupee.format(e.amount + (e.taxAmount ?: 0.0)),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = InPlanBlue
                )
            }
        }
    }
}

// --- Who owes ----------------------------------------------------------------

private fun androidx.compose.foundation.lazy.LazyListScope.balanceListItems(
    balances: List<TripBalance>,
    currentUserId: String?,
    profiles: Map<String, Profile>,
    onSettle: (onResult: (Boolean) -> Unit) -> Unit,
    onShowMessage: (String) -> Unit
) {
    if (balances.isEmpty()) {
        item { EmptyState("No balances yet. Add an expense to see who owes what.", icon = EmptyIconKind.PEOPLE) }
        return
    }
    item {
        BalanceListBody(
            balances = balances,
            currentUserId = currentUserId,
            profiles = profiles,
            onSettle = onSettle,
            onShowMessage = onShowMessage
        )
    }
}

@Composable
private fun BalanceListBody(
    balances: List<TripBalance>,
    currentUserId: String?,
    profiles: Map<String, Profile>,
    onSettle: (onResult: (Boolean) -> Unit) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val rupee = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val context = LocalContext.current
    var isSettling by remember { mutableStateOf(false) }

    fun settleWithFeedback() {
        if (isSettling) return
        isSettling = true
        onSettle { success ->
            isSettling = false
            if (success) onShowMessage("Marked as settled ✓")
        }
    }

    var pendingSettlement by remember { mutableStateOf<Pair<Double?, String>?>(null) }
    var noUpiForName by remember { mutableStateOf<String?>(null) }

    val myOwedAmount = balances
        .firstOrNull { it.userId == currentUserId && it.netBalance < 0 }
        ?.netBalance
        ?.let { kotlin.math.abs(it) }

    val iOwe = myOwedAmount != null

    fun launchUpiPayment(amount: Double?, payeeUpiId: String?, payeeName: String) {
        if (!payeeUpiId.isNullOrBlank()) {
            val builder = android.net.Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", payeeUpiId)
                .appendQueryParameter("pn", payeeName)
                .appendQueryParameter("cu", "INR")
                .appendQueryParameter("tn", "InPlan trip settlement")
            if (amount != null && amount > 0) {
                builder.appendQueryParameter("am", "%.2f".format(amount))
            }
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, builder.build()))
                pendingSettlement = amount to payeeName
            } catch (e: android.content.ActivityNotFoundException) {
                noUpiForName = payeeName
            }
        } else {
            noUpiForName = payeeName
        }
    }

    Column {
        balances.forEach { b ->
            val owed = b.netBalance >= 0
            val isMe = b.userId == currentUserId
            val hasNonZeroBalance = kotlin.math.abs(b.netBalance) >= 0.01
            val isPayable = !isMe && owed && hasNonZeroBalance && iOwe
            val statusColor = if (owed) GetsBackGreen else OwesRed

            Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 6.dp, shape = CardShape, ambientColor = ShadowSoft, spotColor = ShadowSoft)
                        .clip(CardShape)
                        .background(InPlanWhite)
                        .then(
                            if (!isMe) Modifier.clickable {
                                when {
                                    isPayable -> launchUpiPayment(myOwedAmount, profiles[b.userId]?.upiId, b.fullName)
                                    !iOwe -> onShowMessage("You don't currently owe anything — nothing to pay.")
                                    !hasNonZeroBalance -> onShowMessage("You and ${b.fullName} are already settled up — nothing to pay.")
                                    else -> onShowMessage("${b.fullName} owes money too — nothing to pay them.")
                                }
                            } else Modifier
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InitialsAvatar(name = b.fullName, ringColor = statusColor)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(b.fullName, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, color = TextPrimary)
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(statusColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 9.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        if (owed) "GETS BACK" else "OWES",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                        Text(
                            rupee.format(kotlin.math.abs(b.netBalance)),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = statusColor
                        )
                    }

                    if (isPayable) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = HairlineDivider)
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = InPlanBlue,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Tap to pay ${b.fullName} via UPI",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InPlanBlue
                            )
                        }
                    }

                    if (!owed && isMe) {
                        Spacer(Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SheetFieldShape)
                                .border(1.5.dp, CardBorder, SheetFieldShape)
                                .clickable(enabled = !isSettling) { settleWithFeedback() }
                                .padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSettling) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = TextSecondary
                                )
                            } else {
                                Text("Mark as settled manually", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }

    pendingSettlement?.let { (amount, payeeName) ->
        AlertDialog(
            onDismissRequest = { pendingSettlement = null },
            containerColor = CardSurface,
            title = { Text("Did the payment go through?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (amount != null) {
                        "You started a UPI payment of ${rupee.format(amount)} to $payeeName. " +
                                "Only confirm once it's actually gone through — this updates the ledger for everyone on the trip."
                    } else {
                        "You started a UPI payment to $payeeName. " +
                                "Only confirm once it's actually gone through — this updates the ledger for everyone on the trip."
                    },
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingSettlement = null
                    settleWithFeedback()
                }) { Text("Yes, mark settled", color = InPlanBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { pendingSettlement = null }) { Text("Not yet", color = TextSecondary) }
            }
        )
    }

    noUpiForName?.let { name ->
        AlertDialog(
            onDismissRequest = { noUpiForName = null },
            containerColor = CardSurface,
            title = { Text("Can't pay $name via UPI", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "$name hasn't linked a UPI ID yet, or no UPI app was found on this device. " +
                            "Pay them another way, then mark it settled manually.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    noUpiForName = null
                    settleWithFeedback()
                }) { Text("Mark as settled", color = InPlanBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { noUpiForName = null }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun InitialsAvatar(name: String, ringColor: Color) {
    val initials = name.trim().split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(InPlanBlue, InPlanBlueLight)))
            .border(2.dp, ringColor.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = InPlanWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// --- Polls -------------------------------------------------------------------

private fun androidx.compose.foundation.lazy.LazyListScope.pollListItems(
    polls: List<PollWithVotes>,
    currentUserId: String?,
    onVote: (pollId: String, isIn: Boolean) -> Unit,
    onLock: (pollId: String) -> Unit,
    onCancel: (pollId: String) -> Unit,
    onConvertClick: (PollWithVotes) -> Unit
) {
    val rupee = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    if (polls.isEmpty()) {
        item {
            EmptyState(
                "No polls yet. Start one before you order food or a cab.\nTap + to create one.",
                icon = EmptyIconKind.VOTE
            )
        }
        return
    }
    items(polls) { pv ->
        val poll = pv.poll
        val myVote = pv.votes.find { it.userId == currentUserId }?.isIn ?: false
        val accentColor = when (poll.status) {
            "open" -> InPlanBlue
            "locked" -> InPlanBlueLight
            "converted" -> GetsBackGreen
            "cancelled" -> OwesRed
            else -> CardBorder
        }
        Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 6.dp, shape = CardShape, ambientColor = ShadowSoft, spotColor = ShadowSoft)
                    .clip(CardShape)
                    .background(InPlanWhite)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(poll.title, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, color = TextPrimary)
                    PollStatusPill(poll.status)
                }
                Spacer(Modifier.height(8.dp))
                val estimateText = poll.estimatedAmount?.let { "Estimated ${rupee.format(it)}  ·  " } ?: ""
                Text("$estimateText${pv.inCount} in", fontSize = 12.5.sp, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                // Lightweight visual read on turnout — a thin accent track
                // instead of just a number, so the card carries some signal
                // even at a glance.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(HairlineDivider)
                ) {
                    val fraction = if (pv.votes.isNotEmpty()) (pv.inCount.toFloat() / pv.votes.size).coerceIn(0f, 1f) else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .clip(RoundedCornerShape(50))
                            .background(accentColor)
                    )
                }
                Spacer(Modifier.height(14.dp))
                when (poll.status) {
                    "open" -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PollActionButton(label = "I'm in", filled = true, enabled = !myVote, onClick = { onVote(poll.id, true) })
                        PollActionButton(label = "Out", filled = false, enabled = myVote, onClick = { onVote(poll.id, false) })
                        TextButton(onClick = { onLock(poll.id) }) { Text("Lock", color = InPlanBlue, fontSize = 12.5.sp) }
                        TextButton(onClick = { onCancel(poll.id) }) { Text("Cancel", color = TextSecondary, fontSize = 12.5.sp) }
                    }
                    "locked" -> Column {
                        Text("Locked — ordering in progress", color = ConvertedAmber, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (poll.estimatedAmount != null) {
                                PollActionButton(label = "Convert to expense", filled = true, enabled = true, onClick = { onConvertClick(pv) })
                            }
                            TextButton(onClick = { onCancel(poll.id) }) { Text("Cancel instead", color = TextSecondary, fontSize = 12.5.sp) }
                        }
                    }
                    "converted" -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = GetsBackGreen, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Converted to expense", color = GetsBackGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    "cancelled" -> Text("Cancelled", color = ErrorRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PollStatusPill(status: String) {
    val (bg, fg, label) = when (status) {
        "open" -> Triple(InPlanBlue.copy(alpha = .12f), InPlanBlue, "OPEN")
        "locked" -> Triple(InPlanBlueLight.copy(alpha = .12f), InPlanBlueLight, "LOCKED")
        "converted" -> Triple(GetsBackGreen.copy(alpha = .12f), GetsBackGreen, "CONVERTED")
        "cancelled" -> Triple(OwesRed.copy(alpha = 0.14f), CancelledOrange, "CANCELLED")
        else -> Triple(CardBorder, TextSecondary, status.uppercase())
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(label, fontFamily = FontFamily.Monospace, color = fg, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PollActionButton(label: String, filled: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, label = "pollBtnScale")

    if (filled) {
        Box(
            modifier = Modifier
                .scale(scale)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (enabled) Brush.horizontalGradient(listOf(InPlanBlue, InPlanBlueLight))
                    else Brush.linearGradient(listOf(CardBorder, CardBorder))
                )
                .clickable(interactionSource = interactionSource, indication = null, enabled = enabled) { onClick() }
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = if (enabled) InPlanWhite else TextSecondary)
        }
    } else {
        Box(
            modifier = Modifier
                .scale(scale)
                .clip(RoundedCornerShape(12.dp))
                .background(CardSurface)
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .clickable(interactionSource = interactionSource, indication = null, enabled = enabled) { onClick() }
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = if (enabled) OwesRed else TextSecondary)
        }
    }
}

// --- Empty state -------------------------------------------------------------
// Custom-drawn glyphs instead of a Material icon or emoji, so the empty state
// carries the app's own visual signature. Each icon is built purely from
// Canvas primitives and echoes the dashed flight-path stroke style used in
// HeaderPathBackdrop, so it visually rhymes with the rest of the screen.

private enum class EmptyIconKind { RECEIPT, PEOPLE, VOTE }

@Composable
private fun EmptyStateIcon(kind: EmptyIconKind, modifier: Modifier = Modifier) {
    val strokeColor = InPlanBlue
    Canvas(modifier = modifier.size(30.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)

        when (kind) {
            EmptyIconKind.RECEIPT -> {
                // Ticket body with a torn/zigzag bottom edge.
                val body = Path().apply {
                    moveTo(w * 0.18f, h * 0.08f)
                    lineTo(w * 0.82f, h * 0.08f)
                    lineTo(w * 0.82f, h * 0.78f)
                    // zigzag tear along the bottom
                    val zigW = w * 0.64f / 6f
                    var x = w * 0.82f
                    var down = true
                    repeat(6) {
                        x -= zigW
                        lineTo(x, if (down) h * 0.92f else h * 0.78f)
                        down = !down
                    }
                    close()
                }
                drawPath(body, color = strokeColor, style = stroke)
                // content lines inside the ticket
                listOf(0.32f, 0.46f, 0.60f).forEach { fy ->
                    drawLine(
                        color = strokeColor.copy(alpha = 0.55f),
                        start = Offset(w * 0.30f, h * fy),
                        end = Offset(w * 0.70f, h * fy),
                        strokeWidth = 1.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
            EmptyIconKind.PEOPLE -> {
                // Two overlapping circles connected by a low arc — a
                // minimal "shared group" mark instead of a literal figure.
                drawCircle(color = strokeColor, radius = h * 0.16f, center = Offset(w * 0.38f, h * 0.32f), style = stroke)
                drawCircle(color = strokeColor, radius = h * 0.16f, center = Offset(w * 0.62f, h * 0.32f), style = stroke)
                val arc = Path().apply {
                    moveTo(w * 0.18f, h * 0.82f)
                    cubicTo(w * 0.18f, h * 0.55f, w * 0.82f, h * 0.55f, w * 0.82f, h * 0.82f)
                }
                drawPath(arc, color = strokeColor, style = stroke)
            }
            EmptyIconKind.VOTE -> {
                // A checkmark inside a rounded box — "cast your vote".
                drawRoundRect(
                    color = strokeColor,
                    topLeft = Offset(w * 0.16f, h * 0.16f),
                    size = Size(w * 0.68f, h * 0.68f),
                    cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
                    style = stroke
                )
                val check = Path().apply {
                    moveTo(w * 0.34f, h * 0.52f)
                    lineTo(w * 0.46f, h * 0.64f)
                    lineTo(w * 0.68f, h * 0.38f)
                }
                drawPath(check, color = strokeColor, style = stroke)
            }
        }
    }
}

@Composable
private fun EmptyState(message: String, icon: EmptyIconKind = EmptyIconKind.RECEIPT) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, bottom = 32.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(elevation = 8.dp, shape = CircleShape, ambientColor = ShadowSoft, spotColor = ShadowSoft)
                .clip(CircleShape)
                .background(BlueTint),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateIcon(kind = icon)
        }
        Spacer(Modifier.height(16.dp))
        Text(message, color = TextSecondary, textAlign = TextAlign.Center, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

private val EXPENSE_CATEGORIES = listOf("cab", "food", "stay", "shopping", "other")

@Composable
private fun CategoryPicker(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EXPENSE_CATEGORIES.forEach { cat ->
            val isSelected = cat == selected
            val (icon, _, _) = categoryVisuals(cat)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .then(
                        if (isSelected) Modifier.background(Brush.horizontalGradient(listOf(InPlanBlue, InPlanBlueLight)))
                        else Modifier.background(CardSurface).border(1.dp, CardBorder, RoundedCornerShape(50))
                    )
                    .clickable { onSelect(cat) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) InPlanWhite else TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    cat.replaceFirstChar { it.uppercase() },
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) InPlanWhite else TextSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetScaffold(
    title: String,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BgBase,
        dragHandle = {
            Box(Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                Box(
                    Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(CardBorder)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
        ) {
            Text(title, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = TextPrimary)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, fontSize = 14.sp, color = TextSecondary)
            }
            Spacer(Modifier.height(16.dp))
            content()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary
    )
}

@Composable
private fun AddExpenseSheet(
    tripMembers: List<String>,
    onDismiss: () -> Unit,
    onAdd: (title: String, category: String, amount: Double, tax: Double, splitAmong: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("cab") }
    var amount by remember { mutableStateOf("") }
    var tax by remember { mutableStateOf("0") }
    var error by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    SheetScaffold(title = "Add expense", onDismiss = onDismiss) {
        SheetField(value = title, onChange = { title = it }, label = "What was it? e.g. Cab to station")
        Spacer(Modifier.height(16.dp))
        SectionLabel("Category")
        Spacer(Modifier.height(8.dp))
        CategoryPicker(selected = category, onSelect = { category = it })
        Spacer(Modifier.height(16.dp))
        SheetField(value = amount, onChange = { amount = it }, label = "Amount (₹)", keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
        Spacer(Modifier.height(10.dp))
        SheetField(value = tax, onChange = { tax = it }, label = "Tax / extra charges (₹)", keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
        Spacer(Modifier.height(18.dp))

        if (error != null) {
            Text(error!!, color = ErrorRed, fontSize = 12.5.sp)
            Spacer(Modifier.height(8.dp))
        }

        SheetPrimaryButton(label = if (isSubmitting) "Adding…" else "Add expense") {
            if (isSubmitting) return@SheetPrimaryButton
            val amt = amount.toDoubleOrNull() ?: 0.0
            val taxAmt = tax.toDoubleOrNull() ?: 0.0
            when {
                title.isBlank() -> error = "Please enter what the expense was for"
                amt <= 0 -> error = "Please enter a valid amount"
                tripMembers.isEmpty() -> error = "No trip members found to split with"
                else -> {
                    error = null
                    isSubmitting = true
                    onAdd(title, category, amt, taxAmt, tripMembers)
                }
            }
        }
    }
}

@Composable
private fun CreatePollSheet(
    onDismiss: () -> Unit,
    onCreate: (title: String, estimatedAmount: Double?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var estimatedAmount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    SheetScaffold(title = "New poll", subtitle = "Gauge who's in before you commit to spending.", onDismiss = onDismiss) {
        SheetField(value = title, onChange = { title = it }, label = "What's the plan? e.g. Ordering Biryani")
        Spacer(Modifier.height(10.dp))
        SheetField(
            value = estimatedAmount, onChange = { estimatedAmount = it },
            label = "Estimated amount (₹) — leave blank if no money involved",
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        )
        Spacer(Modifier.height(18.dp))

        if (error != null) {
            Text(error!!, color = ErrorRed, fontSize = 12.5.sp)
            Spacer(Modifier.height(8.dp))
        }

        SheetPrimaryButton(label = "Start poll") {
            val amt = estimatedAmount.toDoubleOrNull()
            when {
                title.isBlank() -> error = "Please describe the plan"
                else -> {
                    error = null
                    onCreate(title, amt)
                }
            }
        }
    }
}

@Composable
private fun ConvertPollSheet(
    pollWithVotes: PollWithVotes,
    profileNames: Map<String, String>,
    onDismiss: () -> Unit,
    onConvert: (title: String, category: String, finalAmount: Double, paidByUserId: String) -> Unit
) {
    val poll = pollWithVotes.poll
    val inUserIds = pollWithVotes.votes.filter { it.isIn }.map { it.userId }
    var finalAmount by remember { mutableStateOf(poll.estimatedAmount?.toInt()?.toString() ?: "") }
    var paidBy by remember { mutableStateOf(poll.createdBy) }
    var category by remember { mutableStateOf("other") }
    var error by remember { mutableStateOf<String?>(null) }

    fun nameFor(uid: String) = profileNames[uid] ?: uid

    SheetScaffold(title = "Convert to expense", subtitle = poll.title, onDismiss = onDismiss) {
        SheetField(
            value = finalAmount, onChange = { finalAmount = it },
            label = "Final amount (₹) — what you actually paid",
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        )
        Spacer(Modifier.height(16.dp))
        SectionLabel("Category")
        Spacer(Modifier.height(8.dp))
        CategoryPicker(selected = category, onSelect = { category = it })
        Spacer(Modifier.height(16.dp))
        SectionLabel("Who paid?")
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SheetFieldShape)
                .background(InPlanWhite)
                .border(1.dp, CardBorder, SheetFieldShape)
                .padding(vertical = 4.dp)
        ) {
            inUserIds.forEachIndexed { index, uid ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { paidBy = uid }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    RadioButton(selected = paidBy == uid, onClick = { paidBy = uid }, colors = RadioButtonDefaults.colors(selectedColor = InPlanBlue))
                    Text(
                        if (uid == poll.createdBy) "${nameFor(uid)} (poll creator)" else nameFor(uid),
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }
                if (index != inUserIds.lastIndex) {
                    HorizontalDivider(color = HairlineDivider, modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        val estimatedAmt = poll.estimatedAmount
        val enteredAmt = finalAmount.toDoubleOrNull() ?: 0.0
        if (estimatedAmt != null && enteredAmt > estimatedAmt * 1.2) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ErrorRed.copy(alpha = 0.08f))
                    .padding(12.dp)
            ) {
                Text(
                    "This is more than 20% over the estimate — everyone who's in will need to approve it before it's added to the ledger.",
                    color = ErrorRed,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(10.dp))
        }

        if (error != null) {
            Text(error!!, color = ErrorRed, fontSize = 12.5.sp)
            Spacer(Modifier.height(8.dp))
        }

        SheetPrimaryButton(label = "Convert to expense") {
            when {
                enteredAmt <= 0 -> error = "Please enter a valid final amount"
                paidBy.isBlank() -> error = "Please select who paid"
                else -> {
                    error = null
                    onConvert(poll.title, category, enteredAmt, paidBy)
                }
            }
        }
    }
}

@Composable
private fun SheetField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontSize = 13.sp) },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        shape = SheetFieldShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = InPlanBlue,
            unfocusedBorderColor = CardBorder,
            focusedContainerColor = InPlanWhite,
            unfocusedContainerColor = InPlanWhite,
            focusedLabelColor = InPlanBlue,
            unfocusedLabelColor = TextSecondary,
            cursorColor = InPlanBlue
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SheetPrimaryButton(label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "primaryBtnScale")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .height(54.dp)
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(16.dp), spotColor = InPlanBlue.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(InPlanBlue, InPlanBlueLight)))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = InPlanWhite)
    }
}

@Composable
private fun ModernFab(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, label = "fabScale")
    val elevation by animateDpAsState(if (pressed) 6.dp else 16.dp, label = "fabElevation")

    Box(
        modifier = Modifier
            .size(60.dp)
            .scale(scale)
            .shadow(elevation = elevation, shape = RoundedCornerShape(20.dp), spotColor = InPlanBlue.copy(alpha = 0.55f))
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(InPlanBlue, InPlanBlueLight)))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = InPlanWhite)
    }
}