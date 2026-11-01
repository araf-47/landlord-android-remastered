package com.landlord.android.core.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ConversationDao {

    @Query("SELECT * FROM conversations ORDER BY lastMessageTime DESC")
    LiveData<List<ConversationEntity>> observeAll();

    @Query("SELECT * FROM conversations ORDER BY lastMessageTime DESC")
    List<ConversationEntity> getAll();

    @Query("SELECT * FROM conversations WHERE localId = :localId LIMIT 1")
    LiveData<ConversationEntity> getById(String localId);

    @Query("SELECT * FROM conversations WHERE localId = :localId LIMIT 1")
    ConversationEntity getByIdSync(String localId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ConversationEntity conversation);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ConversationEntity> conversations);

    @Update
    void update(ConversationEntity conversation);

    @Delete
    void delete(ConversationEntity conversation);

    @Query("DELETE FROM conversations")
    void deleteAll();
}
