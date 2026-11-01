package com.landlord.android.core.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "maintenance_tickets",
    indices = {@Index(value = "unitId"), @Index(value = "tenantId")}
)
public class MaintenanceTicketEntity {

    @PrimaryKey
    @NonNull
    public String localId = "";

    public String unitId;
    public String tenantId;
    public String title;
    public String description;
    public String status; // "pending", "in_progress", "resolved"
    public String priority; // "low", "medium", "high"
    public long createdAt;
}
