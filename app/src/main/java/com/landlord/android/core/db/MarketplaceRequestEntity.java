package com.landlord.android.core.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "marketplace_requests",
    indices = {@Index(value = "unitId")}
)
public class MarketplaceRequestEntity {

    @PrimaryKey
    @NonNull
    public String localId = "";

    public String unitId;
    public String applicantName;
    public String applicantPhone;
    public String tenantId;
    public String message;
    public String status; // "pending", "approved", "rejected"
    public long createdAt;
}
