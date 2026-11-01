package com.landlord.android.core.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "properties")
public class PropertyEntity {

    @PrimaryKey
    @NonNull
    public String localId = "";

    public String name;
    public String address;
    public String district;
    public String area;
    public String propertyType; // apartment, commercial, house
    public long landlordId;
    public long createdAt;
}
