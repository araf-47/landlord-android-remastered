package com.landlord.android.feature.messages;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.db.ConversationEntity;
import com.landlord.android.core.db.MessageEntity;
import java.util.List;

public class MessagesViewModel extends AndroidViewModel {

    private final MessageRepository repository;

    public MessagesViewModel(@NonNull Application application) {
        super(application);
        this.repository = new MessageRepository(application);
    }

    public LiveData<List<ConversationEntity>> getConversations() {
        return repository.observeConversations();
    }

    public LiveData<List<MessageEntity>> getMessages(String conversationId) {
        return repository.observeMessages(conversationId);
    }

    public void sendMessage(String conversationId, String content) {
        repository.sendMessage(conversationId, content);
    }
}
