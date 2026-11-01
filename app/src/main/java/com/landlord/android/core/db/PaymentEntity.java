package com.landlord.android.core.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "payments",
    indices = {@Index(value = "tenantId"), @Index(value = "invoiceId")}
)
public class PaymentEntity {

    @PrimaryKey
    @NonNull
    public String localId = "";

    public String tenantId;
    public String invoiceId;
    public double amount;
    public String method; // "cash", "bank", "mobile", "online"
    public String status; // "confirmed", "pending", "rejected"
    public String date; // "2026-10-15"
    public String transactionRef;
    public long createdAt;
}
