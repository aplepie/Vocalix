package com.vocalix.app.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.vocalix.app.database.entity.Recording;

import java.util.List;

@Dao
public interface RecordingDao {

    @Query("SELECT * FROM recordings")
    List<Recording> getAll();

    @Query("SELECT * FROM recordings WHERE id = :id")
    Recording getById(long id);

    @Insert
    void insert(Recording recording);

    @Update
    void update(Recording recording);

    @Query("DELETE FROM recordings WHERE id = :id")
    void deleteById(int id);
}
