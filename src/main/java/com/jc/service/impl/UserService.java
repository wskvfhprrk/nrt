package com.jc.service.impl;

import com.jc.entity.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private static final Map<Long, User> DATABASE = new HashMap<>();

    static {
        DATABASE.put(1L, new User(1L, "John Doe"));
        DATABASE.put(2L, new User(2L, "Jane Doe"));
    }

    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        simulateSlowService();
        return DATABASE.get(id);
    }

    // Simulate a slow service call
    private void simulateSlowService() {
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}
