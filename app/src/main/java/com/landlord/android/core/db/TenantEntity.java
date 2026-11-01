package com.landlord.android.core.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "tenants",
    indices = {@Index(value = "unitId")}
)
public class TenantEntity {

    @PrimaryKey
    @NonNull
    public String localId = "";

    public String name;
    public String phone;
    public String email;
    public String nationalId;
    public String unitId;
    public String status; // "active" or "inactive"
    public long createdAt;
}
