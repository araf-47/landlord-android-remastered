package com.landlord.android.core.common;

import android.content.Context;
import android.content.Intent;
import com.landlord.android.core.db.PaymentEntity;

public final class ReceiptHelper {

    private ReceiptHelper() {}

    public static String buildReceiptText(PaymentEntity payment, String tenantName, String unitNumber) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════\n");
        sb.append("       LANDLORD RENT RECEIPT       \n");
        sb.append("═══════════════════════════════════\n\n");
        sb.append("Receipt ID: ").append(payment.localId).append("\n");
        sb.append("Date: ").append(payment.date).append("\n");
        sb.append("Tenant: ").append(tenantName != null ? tenantName : "Tenant").append("\n");
        sb.append("Unit: ").append(unitNumber != null ? unitNumber : "Assigned Unit").append("\n");
        sb.append("Amount Paid: ৳").append(String.format("%,.2f", payment.amount)).append("\n");
        sb.append("Payment Method: ").append(payment.method.toUpperCase()).append("\n");
        if (payment.transactionRef != null && !payment.transactionRef.isEmpty()) {
            sb.append("Reference: ").append(payment.transactionRef).append("\n");
        }
        sb.append("Status: ").append(payment.status.toUpperCase()).append("\n\n");
        sb.append("═══════════════════════════════════\n");
        sb.append("Thank you for your prompt payment!\n");
        sb.append("═══════════════════════════════════\n");
        return sb.toString();
    }

    public static void shareReceipt(Context context, String receiptText) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, receiptText);
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, "Share Rent Receipt");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(shareIntent);
    }
}
