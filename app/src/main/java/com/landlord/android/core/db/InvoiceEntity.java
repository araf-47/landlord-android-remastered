package com.landlord.android.core.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "invoices",
    indices = {@Index(value = "tenantId"), @Index(value = "unitId"), @Index(value = "period")}
)
public class InvoiceEntity {

    @PrimaryKey
    @NonNull
    public String localId = "";

    public String tenantId;
    public String unitId;
    public String period; // "2026-10"
    public double rent;
    public double utilityTotal;
    public String utilityItemsJson;
    public double prevUnpaidRolled;
    public double amount;
    public double balance;
    public String status; // "unpaid", "partial", "paid"
    public String dueDate;
    public long createdAt;
}
