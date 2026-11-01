package com.landlord.android.core.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "expenses",
    indices = {@Index(value = "propertyId"), @Index(value = "tenantId")}
)
public class ExpenseEntity {

    @PrimaryKey
    @NonNull
    public String localId = "";

    public String propertyId;
    public String category; // "Maintenance", "Utility", "Tax", "Cleaning", "Other"
    public String description;
    public double amount;
    public String bearer; // "landlord" or "tenant"
    public String tenantId;
    public String date;
    public long createdAt;
}
