package com.example.inplan.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.inplan.ui.theme.*
import com.example.inplan.viewmodel.AuthState
import com.example.inplan.viewmodel.AuthViewModel

// --- Local accent colors, shared visual language with TripListScreen -------
private val PerforationLine = Color(0xFFD9DDEE)
private val AmberAccent = Color(0xFFF0A23A)
private val AmberDeep = Color(0xFFB9701E)
private val InkFaint = Color(0xFF9498B8)
private val FieldIdle = Color(0xFFE3E6F3)
private val FieldSurface = Color(0xFFF7F8FC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    vm: AuthViewModel = hiltViewModel(),
    onDone: () -> Unit = {},
    onLoggedOut: () -> Unit = {}
) {
    var upiId by remember { mutableStateOf("") }
    var hasPrefilled by remember { mutableStateOf(false) }
    var touched by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    val updateState by vm.upiUpdateState.collectAsState()
    val currentUpiId by vm.currentUpiId.collectAsState()

    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(Unit) {
        vm.loadCurrentUpiId()
        contentVisible = true
    }

    LaunchedEffect(currentUpiId) {
        if (!hasPrefilled && currentUpiId != null) {
            upiId = currentUpiId!!
            hasPrefilled = true
        }
    }

    fun isValidUpiId(value: String): Boolean =
        Regex("^[\\w.\\-]{2,}@[a-zA-Z]{2,}$").matches(value.trim())

    val trimmed = upiId.trim()
    val showValidationError = touched && trimmed.isNotEmpty() && !isValidUpiId(trimmed)
    val serverError = (updateState as? AuthState.Error)?.message
    val fieldError = serverError ?: if (showValidationError) "That doesn't look like a valid UPI ID" else null
    val isSuccess = updateState is AuthState.Success
    val isLoading = updateState is AuthState.Loading

    LaunchedEffect(updateState) {
        if (isSuccess) onDone()
    }

    Scaffold(
        containerColor = BgBase,
        bottomBar = {
            BottomActionBar(
                enabled = trimmed.isNotEmpty(),
                isLoading = isLoading,
                onContinue = {
                    touched = true
                    if (isValidUpiId(trimmed)) {
                        vm.updateUpiId(trimmed)
                    }
                },
                onSkip = onDone,
                onLogoutClick = { showLogoutConfirm = true }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            FlightPathBackdrop(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
                    .height(200.dp)
                    .align(Alignment.TopStart)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {

                Spacer(Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(tween(400)) + slideInVertically(
                        initialOffsetY = { it / 6 },
                        animationSpec = tween(400)
                    )
                ) {
                    Column {

                        // Understated status badge instead of a heavy stamp
                        Box(
                            modifier = Modifier
                                .align(Alignment.Start)
                                .clip(RoundedCornerShape(50))
                                .background(AmberAccent.copy(alpha = 0.10f))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "One step left",
                                color = AmberDeep,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.2.sp
                            )
                        }

                        Spacer(Modifier.height(18.dp))

                        Text(
                            text = "Get paid back\ninstantly",
                            fontFamily = FontFamily.Serif,
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Link a UPI ID so settlements land straight in your account.",
                            fontSize = 14.5.sp,
                            color = TextSecondary,
                            lineHeight = 21.sp
                        )

                        Spacer(Modifier.height(28.dp))

                        StatusCard(hasUpi = !upiId.isBlank())

                        Spacer(Modifier.height(28.dp))

                        Text(
                            text = "YOUR UPI ID",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.8.sp,
                            color = TextSecondary
                        )

                        Spacer(Modifier.height(8.dp))

                        UpiField(
                            value = upiId,
                            onValueChange = {
                                upiId = it
                            },
                            onFocusLost = { touched = true },
                            isError = fieldError != null,
                            isSuccess = isSuccess,
                            interactionSource = interactionSource
                        )

                        AnimatedVisibility(visible = fieldError != null) {
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = fieldError.orEmpty(),
                                    color = ErrorRed,
                                    fontSize = 12.5.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = InkFaint,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Encrypted end-to-end · you can change this anytime",
                                color = InkFaint,
                                fontSize = 11.5.sp
                            )
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor = CardSurface,
            title = { Text("Log out?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "You'll need to sign in again to see your trips.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        vm.signOut { onLoggedOut() }
                    }
                ) {
                    Text("Log out", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

/**
 * Compact status card. Swaps icon/copy based on whether a UPI ID is present
 * so the same real estate does double duty instead of always saying
 * "not connected yet."
 */
@Composable
private fun StatusCard(hasUpi: Boolean) {
    val tint = if (hasUpi) GetsBackGreen else InPlanBlue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(tint.copy(alpha = 0.07f))
            .border(1.dp, tint.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column {
            Text(
                text = if (hasUpi) "Almost ready" else "No UPI ID yet",
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = TextPrimary
            )
            Text(
                text = if (hasUpi) "Save it below to finish up" else "Add one to receive settlements",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Single-purpose UPI input. Border and label color animate with focus/error
 * state instead of relying on a static outline, and there's no separate
 * "ticket" chrome competing with the modern M3 field styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpiField(
    value: String,
    onValueChange: (String) -> Unit,
    onFocusLost: () -> Unit,
    isError: Boolean,
    isSuccess: Boolean,
    interactionSource: MutableInteractionSource
) {
    var wasFocused by remember { mutableStateOf(false) }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        if (wasFocused && !isFocused) onFocusLost()
        wasFocused = isFocused
    }

    val borderColor = when {
        isError -> ErrorRed
        isSuccess -> GetsBackGreen
        isFocused -> InPlanBlue
        else -> FieldIdle
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isFocused) 6.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = InPlanBlue.copy(alpha = 0.25f)
            ),
        interactionSource = interactionSource,
        placeholder = {
            Text(
                "yourname@upi",
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp,
                color = InkFaint
            )
        },
        textStyle = LocalTextStyle.current.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 15.sp,
            color = TextPrimary
        ),
        singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            imeAction = ImeAction.Done
        ),
        trailingIcon = {
            if (isSuccess) {
                Icon(Icons.Default.Check, contentDescription = null, tint = GetsBackGreen)
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = FieldSurface,
            unfocusedContainerColor = FieldSurface,
            errorContainerColor = FieldSurface,
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            errorBorderColor = ErrorRed,
            cursorColor = InPlanBlue
        )
    )
}

/**
 * Everything the user needs to act on lives here, pinned to the bottom of
 * the screen via Scaffold's bottomBar — always reachable with zero
 * scrolling regardless of device height or content length.
 */
@Composable
private fun BottomActionBar(
    enabled: Boolean,
    isLoading: Boolean,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgBase)
            .padding(horizontal = 24.dp)
    ) {

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(
                    elevation = if (isLoading || !enabled) 0.dp else 12.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = InPlanBlue.copy(alpha = 0.4f)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (!enabled) Brush.horizontalGradient(listOf(CardBorder, CardBorder))
                    else Brush.horizontalGradient(listOf(InPlanBlue, InPlanBlueLight))
                )
                .then(
                    if (enabled && !isLoading) {
                        Modifier.clickable(onClick = onContinue)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "continue-button-content"
            ) { loading ->
                if (loading) {
                    CircularProgressIndicator(
                        color = InPlanWhite,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Continue",
                            color = if (enabled) InPlanWhite else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (enabled) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = InPlanWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Skip for now", color = TextSecondary, fontWeight = FontWeight.Medium, fontSize = 13.5.sp)
        }

        Spacer(Modifier.height(4.dp))

        TextButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.7f)
        ) {
            Text(
                text = "LOG OUT",
                color = ErrorRed,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.5.sp,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

/**
 * The faint dashed flight-path curve, same motif used behind the trips list
 * header.
 */
@Composable
private fun FlightPathBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(-20f, h * 0.25f)
            cubicTo(
                w * 0.3f, h * 0.02f,
                w * 0.45f, h * 0.5f,
                w * 0.7f, h * 0.32f
            )
            cubicTo(
                w * 0.86f, h * 0.2f,
                w * 1.05f, h * 0.02f,
                w * 1.18f, h * 0.36f
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

        drawCircle(
            color = AmberAccent,
            radius = 3.5.dp.toPx(),
            center = Offset(w * 0.7f, h * 0.32f)
        )
    }
}