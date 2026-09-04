package com.example.inplan.ui



import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inplan.data.util.QrCodeGenerator
import com.example.inplan.data.util.UpiPaymentHelper
import java.util.Locale

/**
 * Settlement flow for a single debt:
 *   1. Primary action: "Pay with UPI app" — direct deep link via UpiPaymentHelper.
 *      This is the normal, standard way to trigger a UPI payment.
 *   2. Secondary action: "Show QR to scan" — for cases where the two people
 *      are together in person and scanning is more convenient than a deep link
 *      (e.g. the payer wants to use their own already-open camera/app).
 *   3. "Copy UPI ID" — plain fallback if neither of the above works for
 *      some reason (no UPI apps resolve, etc).
 *
 * Neither path is framed as bypassing anything — the deep link is a
 * standard Intent.ACTION_VIEW call, and the QR is just an alternate
 * entry point into the exact same upi://pay request.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementBottomSheet(
    payeeName: String,
    payeeUpiId: String,
    amountOwed: Double,
    onDismiss: () -> Unit,
    onMarkSettled: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showQr by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Pay $payeeName", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Text(
                text = "₹${String.format(Locale.US, "%.2f", amountOwed)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            if (!showQr) {
                // --- Primary: standard UPI deep link ---
                Button(
                    onClick = {
                        UpiPaymentHelper.pay(
                            context = context,
                            payeeUpiId = payeeUpiId,
                            payeeName = payeeName,
                            amount = amountOwed
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pay with UPI app")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- Secondary: show QR (for in-person settling) ---
                OutlinedButton(
                    onClick = { showQr = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Show QR to scan instead")
                }
            } else {
                val qrBitmap = remember(payeeUpiId, amountOwed) {
                    QrCodeGenerator.generateUpiQr(
                        payeeUpiId = payeeUpiId,
                        payeeName = payeeName,
                        amount = amountOwed
                    )
                }

                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Scan to pay via UPI",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = "Ask $payeeName to scan this with any UPI app",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                )

                TextButton(onClick = { showQr = false }) {
                    Text("Back to pay with app")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // --- Fallback: copy UPI ID ---
            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Payee UPI ID", payeeUpiId))
                    Toast.makeText(context, "UPI ID copied: $payeeUpiId", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy UPI ID")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- Mark as settled in your own ledger ---
            Button(
                onClick = {
                    onMarkSettled()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Mark as Settled")
            }
        }
    }
}