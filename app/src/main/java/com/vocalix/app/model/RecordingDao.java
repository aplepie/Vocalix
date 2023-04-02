package com.vocalix.app.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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
