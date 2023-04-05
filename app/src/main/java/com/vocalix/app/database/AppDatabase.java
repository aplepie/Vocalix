package com.vocalix.app.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.vocalix.app.database.dao.ExerciseDao;
import com.vocalix.app.database.dao.RecordingDao;
import com.vocalix.app.database.dao.UserDao;
import com.vocalix.app.database.entity.Exercise;
import com.vocalix.app.database.entity.Recording;
import com.vocalix.app.database.entity.User;
import com.vocalix.app.ui.utils.DateConverter;

@Database(entities = {Recording.class, Exercise.class, User.class}, version = 3, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract RecordingDao recordingDao();
    public abstract ExerciseDao vocalExerciseDao();
    public abstract UserDao userDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .fallbackToDestructiveMigration() // Allow destructive migration
                            .build();
                    /*
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database").build();
                     */
                }
            }
        }
        return INSTANCE;
    }
}
