package com.landlord.android.core.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationEntity {

    @PrimaryKey
    @NonNull
    public String localId = "";

    public String title;
    public String body;
    public String category; // "payment", "maintenance", "lead", "system"
    public boolean read;
    public long createdAt;
}
