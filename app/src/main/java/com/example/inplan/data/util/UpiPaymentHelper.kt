package com.example.inplan.data.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import java.util.Locale

/**
 * Standard UPI deep-link payment trigger.
 *
 * Uses Intent.ACTION_VIEW with a "upi://pay" URI, which is the normal,
 * NPCI-documented way third-party apps request a UPI payment. Android
 * shows the user's installed UPI apps in a chooser; the user picks one
 * and completes the payment there. This app never touches the money.
 */
object UpiPaymentHelper {

    fun pay(
        context: Context,
        payeeUpiId: String,
        payeeName: String,
        amount: Double,
        note: String = "Trip settlement"
    ) {
        if (payeeUpiId.isBlank()) {
            Toast.makeText(context, "$payeeName hasn't linked a UPI ID yet", Toast.LENGTH_SHORT).show()
            return
        }

        val formattedAmount = String.format(Locale.US, "%.2f", amount)

        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", payeeUpiId)      // payee address
            .appendQueryParameter("pn", payeeName)       // payee name
            .appendQueryParameter("am", formattedAmount) // amount
            .appendQueryParameter("cu", "INR")           // currency
            .appendQueryParameter("tn", note)            // transaction note
            .build()
        Log.d("UpiDebug", "Final URI: $uri")

        val intent = Intent(Intent.ACTION_VIEW, uri)

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(intent, "Pay with"))
        } else {
            Toast.makeText(context, "No UPI app found on this device", Toast.LENGTH_SHORT).show()
        }
    }
}