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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    // 微信服务接口地址
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;
    /**
     * 微信登录
     * @param UserLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO UserLoginDTO) {


        // 1. 根据登录DTO中的code参数调用微信API获取openid和session_key
       String code = UserLoginDTO.getCode();
       String openid = getOpenid(code);

        // 2. 根据openid查询数据库，判断用户是否已经注册

        if(openid == null || openid.equals("")){
            //throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
            String openId = "MOCK_OPENID_" + code.substring(0, 8) + "_DEV";
            log.info("✅ 使用模拟 openId: {}", openId);
        }
        // 3. 如果用户未注册，创建新用户并保存到数据库
        User user = userMapper.getUserByOpenid(openid);
        if(user == null){
            user = new User();
            user.setOpenid(openid);
            user.setCreateTime(LocalDateTime.now());
            userMapper.insert(user);
            log.info("新用户注册成功，openid：{}", openid);
        }


        // 4. 返回用户信息

        return user;
    }

    /**
     * 获取openid
     * @param code
     * @return
     */
    private String getOpenid(String code){
        log.info("调用微信API获取openid，code：{}", code);
        Map<String,String> map = new HashMap<>();
        map.put("js_code", code);
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);
        log.info("微信 jscode2session 返回结果：{}", json);
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        if (openid == null || openid.isEmpty()) {
            log.error("获取openid失败，微信返回：{}", json);
            throw new LoginFailedException("获取openid失败");
        }
        return openid;
    }
}
