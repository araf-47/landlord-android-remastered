package com.landlord.android.core.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "rental_agreements",
    indices = {@Index(value = "tenantId"), @Index(value = "unitId")}
)
public class RentalAgreementEntity {

    @PrimaryKey
    @NonNull
    public String localId = "";

    public String tenantId;
    public String unitId;
    public String startDate;
    public String terms;
    public double deposit;
    public long createdAt;
}
