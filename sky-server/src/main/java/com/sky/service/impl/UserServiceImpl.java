package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatProperties weChatProperties;
    public static final String WechatHttps = "https://api.weixin.qq.com/sns/jscode2session";
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        log.info("微信登录，参数：{}", userLoginDTO);
        String openid = getOpenId(userLoginDTO.getCode());
        //判断openid是否为空
        if (openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断是否新用户
        User user = userMapper.selectUserByOpenid(openid);
        if (user == null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //返回用户数据
        return user;
    }
    private String getOpenId(String code) {
        //调用微信接口查询微信用户openid
        Map<String, String> map = Map.of(
                "appid", weChatProperties.getAppid(),
                "secret", weChatProperties.getSecret(),
                "js_code", code,
                "grant_type", "authorization_code"
        );
        String json = HttpClientUtil.doGet(WechatHttps, map);
        log.info("微信接口返回数据：{}", json);
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }
}
