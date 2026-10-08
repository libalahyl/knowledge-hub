package com.knowledgehub.account.service;

import com.knowledgehub.account.entity.User;

public interface UserService {

    User getById(Long id);

    User getByUsername(String username);

    User register(String username, String password, String nickname);
}
