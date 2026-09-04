package com.example.inplan.ui.screens.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.inplan.ui.theme.*
import com.example.inplan.viewmodel.TripViewModel

@Composable
fun JoinTripScreen(
    tripId: String,
    onJoined: () -> Unit,
    vm: TripViewModel = hiltViewModel()
) {
    var attempt by remember { mutableStateOf(0) }

    LaunchedEffect(tripId, attempt) {
        vm.clearError()

        vm.joinTrip(
            tripId = tripId,
            onJoined = onJoined
        )
    }

    val errorMessage by vm.errorMessage.collectAsState()
    val joinFailed = errorMessage != null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.weight(1f))

            if (joinFailed) {

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            ErrorRed.copy(alpha = 0.08f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 54.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Couldn't join trip",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = errorMessage
                        ?: "This invite link may have expired or is no longer valid.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        vm.clearError()
                        attempt++
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InPlanBlue
                    )
                ) {
                    Text(
                        text = "Try Again",
                        fontWeight = FontWeight.Bold,
                        color = InPlanWhite
                    )
                }

            } else {

                JoinTripIllustration()

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Preparing your trip",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "We're syncing members, expenses and destination details.",
                    color = TextSecondary,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                CircularProgressIndicator(
                    color = InPlanBlue,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!joinFailed) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardSurface
                    )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "🛡️",
                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {

                            Text(
                                text = "Secure & private",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Your trip data stays protected and synced.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun JoinTripIllustration() {

    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(
                    InPlanBlue.copy(alpha = 0.05f)
                )
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            InPlanBlue,
                            InPlanBlueLight
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "🌎",
                fontSize = 72.sp
            )
        }

        Box(
            modifier = Modifier
                .offset(
                    x = (-65).dp,
                    y = (-45).dp
                )
                .size(44.dp)
                .clip(CircleShape)
                .background(CardSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📍",
                fontSize = 18.sp
            )
        }

        Box(
            modifier = Modifier
                .offset(
                    x = 70.dp,
                    y = (-20).dp
                )
                .size(44.dp)
                .clip(CircleShape)
                .background(CardSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📍",
                fontSize = 18.sp
            )
        }

        Box(
            modifier = Modifier
                .offset(
                    x = (-20).dp,
                    y = 75.dp
                )
                .size(44.dp)
                .clip(CircleShape)
                .background(CardSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📍",
                fontSize = 18.sp
            )
        }

        Card(
            modifier = Modifier
                .offset(
                    x = 55.dp,
                    y = (-70).dp
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardSurface
            )
        ) {

            Text(
                text = "✈️",
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 6.dp
                ),
                fontSize = 18.sp
            )
        }
    }
}