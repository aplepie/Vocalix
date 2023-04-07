package com.vocalix.app.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.vocalix.app.database.entity.Exercise;

import java.util.List;

@Dao
public interface ExerciseDao {
    @Query("SELECT * FROM exercises")
    List<Exercise> getAll();

    @Insert
    void insertAll(Exercise... exercises);
    @Update
    void update(Exercise exercise);

    @Update
    void updateAll(Exercise... exercises);
}
