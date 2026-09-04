package com.example.inplan.data.util



import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.util.Locale

/**
 * Renders a standard UPI payment string as a scannable QR bitmap, for the
 * "show my QR" in-person settlement flow. This is the same pattern used
 * by Splitwise, GPay, and most Indian fintech apps for peer-to-peer
 * settlement — it's a normal UX convenience, not a workaround.
 */
object QrCodeGenerator {

    fun generateUpiQr(
        payeeUpiId: String,
        payeeName: String,
        amount: Double,
        note: String = "InPlan Settlement",
        size: Int = 512
    ): Bitmap {
        val formattedAmount = String.format(Locale.US, "%.2f", amount)
        val uriString = "upi://pay?pa=$payeeUpiId&pn=$payeeName&am=$formattedAmount&cu=INR&tn=$note"
        Log.d("UpiDebug", "QR payload: $uriString")

        val bitMatrix = QRCodeWriter().encode(uriString, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}