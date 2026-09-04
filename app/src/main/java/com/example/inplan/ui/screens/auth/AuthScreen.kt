package com.example.inplan.ui.screens.auth

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.inplan.R
import com.example.inplan.ui.theme.*
import com.example.inplan.viewmodel.AuthState
import com.example.inplan.viewmodel.AuthViewModel

// New brand tokens — add these two to ui/theme/Color.kt alongside the
// existing palette. Everything else below reuses BgBase, CardSurface,
// TextPrimary, TextSecondary, CardBorder, InPlanWhite as already defined.
//   val InPlanInk   = Color(0xFF14182B)
//   val AccentCoral = Color(0xFFFF6B4A)

private val HeaderHeight = 236.dp
private val SeamNotchRadius = 14.dp
private val SeamCornerRadius = 28.dp
val InPlanInk = Color(0xFF14182B)
val AccentCoral = Color(0xFF3070F6)

@Composable
fun AuthScreen(
    onSignedIn: (needsProfileSetup: Boolean) -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading = state is AuthState.Loading

    LaunchedEffect(state) {
        when (val s = state) {
            is AuthState.Success -> onSignedIn(vm.needsProfileSetup.value)
            is AuthState.Error -> snackbarHostState.showSnackbar(
                message = s.message,
                withDismissAction = true
            )
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = InPlanInk
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // Header — quiet, dark, logo only. The visual interest lives in
            // the seam below, not in a gradient here.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HeaderHeight)
                    .background(InPlanInk),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.inplanlogo),
                        contentDescription = "InPlan Logo",
                        modifier = Modifier.height(88.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Plan · Split · Settle",
                        color = InPlanWhite.copy(alpha = 0.75f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Perforation line, sitting just above the tear.
                Canvas(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = SeamNotchRadius + 6.dp)
                        .fillMaxWidth(0.86f)
                        .height(1.dp)
                ) {
                    drawLine(
                        color = InPlanWhite.copy(alpha = 0.28f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 7f), 0f)
                    )
                }
            }

            // Form sheet — overlaps the header by SeamNotchRadius so the
            // torn-notch cutouts reveal the ink navy behind them.
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = HeaderHeight - SeamNotchRadius),
                color = CardSurface,
                shape = TicketSeamShape(
                    cornerRadius = SeamCornerRadius,
                    notchRadius = SeamNotchRadius
                ),
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 26.dp)
                        .padding(top = SeamNotchRadius + 26.dp, bottom = 24.dp)
                ) {

                    Crossfade(targetState = isSignUp, label = "authCopy") { signUp ->
                        Column {
                            Text(
                                text = if (signUp) "Let's get you set up" else "Good to see you again",
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.2).sp,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (signUp) "Takes less than a minute." else "Let's get things in plan.",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(30.dp))

                    if (isSignUp) {
                        TicketField(
                            label = "Full name",
                            value = fullName,
                            onValueChange = { fullName = it }
                        )
                        Spacer(Modifier.height(20.dp))
                    }

                    TicketField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it },
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(20.dp))

                    TicketField(
                        label = "Password",
                        value = password,
                        onValueChange = { password = it },
                        isPassword = true
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AccentCoral,
                                uncheckedColor = CardBorder
                            )
                        )
                        Text(
                            text = "Remember me",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "Forgot password?",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(26.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(
                                elevation = if (isLoading) 0.dp else 10.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = AccentCoral.copy(alpha = 0.4f),
                                spotColor = AccentCoral.copy(alpha = 0.4f)
                            )
                            .background(AccentCoral, RoundedCornerShape(16.dp))
                            .then(
                                if (!isLoading) {
                                    Modifier.clickable {
                                        if (isSignUp) vm.signUp(email, password, fullName)
                                        else vm.signIn(email, password)
                                    }
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = InPlanWhite,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = if (isSignUp) "Create account" else "Log in",
                                color = InPlanWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    TextButton(
                        onClick = { isSignUp = !isSignUp },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isSignUp) "Already have an account? Log in"
                            else "New here? Create an account",
                            color = TextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Underline-style field: label above, hairline below, coral when focused.
 * Deliberately not a boxed OutlinedTextField — keeps the form light and
 * consistent with the ticket-stub language of the header.
 */
@Composable
private fun TicketField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    var focused by remember { mutableStateOf(false) }
    val lineColor by animateColorAsState(
        targetValue = if (focused) AccentCoral else CardBorder,
        label = "fieldLine"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 16.sp, color = TextPrimary),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            cursorBrush = SolidColor(AccentCoral),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused }
                .padding(bottom = 10.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (focused) 2.dp else 1.dp)
                .background(lineColor)
        )
    }
}

/**
 * Rounded-top rect with two semicircle cutouts at the top-left and
 * top-right corners — the "torn ticket" seam where the form sheet
 * overlaps the header.
 */
private class TicketSeamShape(
    private val cornerRadius: Dp,
    private val notchRadius: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val corner = with(density) { cornerRadius.toPx() }
        val notch = with(density) { notchRadius.toPx() }

        val base = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(0f, 0f, size.width, size.height),
                    topLeft = CornerRadius(corner, corner),
                    topRight = CornerRadius(corner, corner),
                    bottomLeft = CornerRadius.Zero,
                    bottomRight = CornerRadius.Zero
                )
            )
        }
        val leftNotch = Path().apply {
            addOval(Rect(center = Offset(0f, 0f), radius = notch))
        }
        val rightNotch = Path().apply {
            addOval(Rect(center = Offset(size.width, 0f), radius = notch))
        }

        val trimmed = Path.combine(PathOperation.Difference, base, leftNotch)
        val final = Path.combine(PathOperation.Difference, trimmed, rightNotch)
        return Outline.Generic(final)
    }
}