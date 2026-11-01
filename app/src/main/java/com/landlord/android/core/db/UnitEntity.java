package com.landlord.android.core.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "units",
    indices = {@Index(value = "propertyId")}
)
public class UnitEntity {

    @PrimaryKey
    @NonNull
    public String localId = "";

    public String propertyId;
    public String unitNumber;
    public double rent;
    public String status; // "vacant" or "occupied"
    public String propertyType; // "apartment", "room", "office"
    public boolean adPaused;
    public String utilityItemsJson; // JSON representation of utility items
    public long createdAt;
}
