package com.landlord.android.core.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "messages",
    indices = {@Index(value = "conversationId")}
)
public class MessageEntity {

    @PrimaryKey
    @NonNull
    public String localId = "";

    public String conversationId;
    public String senderType; // "landlord" or "tenant"
    public String content;
    public long sentAt;
}
