package com.vocalix.app.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.vocalix.app.database.dao.UserDao;
import com.vocalix.app.database.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private final UserDao userDao;
    private final LiveData<User> user;
    private final ExecutorService executorService;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        user = userDao.getUser();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<User> getUser() {
        return user;
    }

    public void insertOrUpdate(User user) {
        executorService.execute(() -> {
            if (userDao.getUserCount() > 0) {
                userDao.updateUser(user);
            } else {
                userDao.insert(user);
            }
        });
    }
}
