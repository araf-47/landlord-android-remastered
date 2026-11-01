package com.landlord.android.feature.messages;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.common.AppExecutors;
import com.landlord.android.core.db.AppDatabase;
import com.landlord.android.core.db.ConversationDao;
import com.landlord.android.core.db.ConversationEntity;
import com.landlord.android.core.db.MessageDao;
import com.landlord.android.core.db.MessageEntity;
import java.util.List;
import java.util.UUID;

public class MessageRepository {

    private final ConversationDao conversationDao;
    private final MessageDao messageDao;

    public MessageRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.conversationDao = db.conversationDao();
        this.messageDao = db.messageDao();
    }

    public LiveData<List<ConversationEntity>> observeConversations() {
        return conversationDao.observeAll();
    }

    public LiveData<ConversationEntity> observeConversation(String conversationId) {
        return conversationDao.getById(conversationId);
    }

    public LiveData<List<MessageEntity>> observeMessages(String conversationId) {
        return messageDao.observeByConversation(conversationId);
    }

    public void sendMessage(String conversationId, String content) {
        AppExecutors.DB.execute(() -> {
            long now = System.currentTimeMillis();

            MessageEntity msg = new MessageEntity();
            msg.localId = "msg-" + UUID.randomUUID().toString().substring(0, 8);
            msg.conversationId = conversationId;
            msg.senderType = "landlord";
            msg.content = content;
            msg.sentAt = now;
            messageDao.insert(msg);

            ConversationEntity conv = conversationDao.getByIdSync(conversationId);
            if (conv != null) {
                conv.lastMessage = content;
                conv.lastMessageTime = now;
                conversationDao.update(conv);
            }
        });
    }

    public void startConversation(String tenantId, String tenantName, String initialMessage) {
        AppExecutors.DB.execute(() -> {
            long now = System.currentTimeMillis();
            String convId = "conv-" + UUID.randomUUID().toString().substring(0, 8);

            ConversationEntity conv = new ConversationEntity();
            conv.localId = convId;
            conv.tenantId = tenantId;
            conv.tenantName = tenantName;
            conv.lastMessage = initialMessage;
            conv.lastMessageTime = now;
            conv.unreadCount = 0;
            conversationDao.insert(conv);

            MessageEntity msg = new MessageEntity();
            msg.localId = "msg-" + UUID.randomUUID().toString().substring(0, 8);
            msg.conversationId = convId;
            msg.senderType = "landlord";
            msg.content = initialMessage;
            msg.sentAt = now;
            messageDao.insert(msg);
        });
    }
}
