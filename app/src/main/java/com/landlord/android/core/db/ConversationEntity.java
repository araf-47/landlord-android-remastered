package com.landlord.android.core.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "conversations")
public class ConversationEntity {

    @PrimaryKey
    @NonNull
    public String localId = "";

    public String tenantId;
    public String tenantName;
    public String lastMessage;
    public long lastMessageTime;
    public int unreadCount;
}
