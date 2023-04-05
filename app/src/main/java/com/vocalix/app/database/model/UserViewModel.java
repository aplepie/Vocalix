package com.vocalix.app.database.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vocalix.app.database.UserRepository;
import com.vocalix.app.database.entity.User;

public class UserViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private LiveData<User> user;

    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        user = userRepository.getUser();
    }

    public LiveData<User> getUser() {
        return user;
    }

    public void insertOrUpdate(User user) {
        userRepository.insertOrUpdate(user);
    }
}
