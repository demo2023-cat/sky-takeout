package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {
    User login(UserLoginDTO userLoginDTO);
}
