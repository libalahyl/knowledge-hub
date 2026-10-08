package com.knowledgehub.account.service;

import com.knowledgehub.account.vo.LoginVO;

public interface AuthService {

    LoginVO login(String username, String password);

    void logout(String token);
}
