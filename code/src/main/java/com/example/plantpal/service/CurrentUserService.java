package com.example.plantpal.service;

import com.example.plantpal.domain.entity.User;
public interface CurrentUserService {

    User getCurrentUser();

    Long getCurrentUserId();

    boolean isAdmin();
}