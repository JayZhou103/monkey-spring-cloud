package com.github.user.service.impl;

import com.github.user.param.LoginParam;
import com.github.user.po.User;

public interface UserService {

	User login(LoginParam param);
	
}