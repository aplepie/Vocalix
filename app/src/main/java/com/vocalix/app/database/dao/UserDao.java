package com.vocalix.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.vocalix.app.database.entity.User;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user WHERE id = 1")
    LiveData<User> getUser();

    @Query("SELECT COUNT(id) FROM user")
    int getUserCount();

    @Insert
    void insert(User user);

    @Update
    void updateUser(User user);
}
