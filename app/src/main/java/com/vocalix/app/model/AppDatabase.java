package com.vocalix.app.model;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.vocalix.app.resources.DateConverter;

@Database(entities = {Recording.class}, version = 1)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract RecordingDao recordingDao();
}
