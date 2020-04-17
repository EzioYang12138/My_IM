package cn.yangjian.im.route.service.impl;

import cn.yangjian.im.common.enums.StatusEnum;
import cn.yangjian.im.common.exception.IMException;

import cn.yangjian.im.common.pojo.IMUserInfo;
import cn.yangjian.im.route.constant.Constant;
import cn.yangjian.im.route.service.AccountService;
import cn.yangjian.im.route.service.UserInfoCacheService;
import cn.yangjian.im.route.vo.req.ChatReqVO;
import cn.yangjian.im.route.vo.req.LoginReqVO;
import cn.yangjian.im.route.vo.res.IMServerResVO;
import cn.yangjian.im.route.vo.res.RegisterInfoResVO;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class AccountServiceRedisImpl implements AccountService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AccountServiceRedisImpl.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserInfoCacheService userInfoCacheService;

    @Autowired
    private OkHttpClient okHttpClient;

    private final MediaType mediaType = MediaType.parse("application/json");

    @Override
    public RegisterInfoResVO register(RegisterInfoResVO info) {
        String key = Constant.ACCOUNT_PREFIX + info.getUserId();

        String name = redisTemplate.opsForValue().get(info.getUserName());
        if (null == name) {
            redisTemplate.opsForValue().set(key, info.getUserName());
        } else {
            long userId = Long.parseLong(name.split(":")[1]);
            info.setUserId(userId);
            info.setUserName(info.getUserName());
        }

        return info;
    }

    @Override
    public StatusEnum login(LoginReqVO loginReqVO) throws Exception {
        //再去Redis里查询
        String key = Constant.ACCOUNT_PREFIX + loginReqVO.getUserId();
        String userName = redisTemplate.opsForValue().get(key);
        if (null == userName || !userName.equals(loginReqVO.getUserName())) {
            return StatusEnum.ACCOUNT_NOT_MATCH;
        }

        //登录成功，保存登录状态
        boolean status = userInfoCacheService.saveAndCheckUserLoginStatus(loginReqVO.getUserId());
        if (!status) {
            //重复登录
            return StatusEnum.REPEAT_LOGIN;
        }

        return StatusEnum.SUCCESS;
    }

    @Override
    public void saveRouteInfo(LoginReqVO loginReqVO, String msg) throws Exception {
        String key = Constant.ROUTE_PREFIX + loginReqVO.getUserId();
        redisTemplate.opsForValue().set(key, msg);
    }

    @Override
    public Map<Long, IMServerResVO> loadRouteRelated() {

        Map<Long, IMServerResVO> routes = new HashMap<>(64);


        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        ScanOptions options = ScanOptions.scanOptions()
                .match(Constant.ROUTE_PREFIX + "*")
                .build();
        Cursor<byte[]> scan = connection.scan(options);

        while (scan.hasNext()) {
            byte[] next = scan.next();
            String key = new String(next, StandardCharsets.UTF_8);
            LOGGER.info("key={}", key);
            parseServerInfo(routes, key);

        }
        try {
            scan.close();
        } catch (IOException e) {
            LOGGER.error("IOException", e);
        }

        return routes;
    }

    private void parseServerInfo(Map<Long, IMServerResVO> routes, String key) {
        long userId = Long.parseLong(key.split(":")[1]);
        String value = redisTemplate.opsForValue().get(key);
        String[] server = value.split(":");
        IMServerResVO imServerResVO = new IMServerResVO(server[0], Integer.parseInt(server[1]), Integer.parseInt(server[2]));
        routes.put(userId, imServerResVO);
    }

    @Override
    public IMServerResVO loadRouteRelatedByUserId(Long userId) {
        String value = redisTemplate.opsForValue().get(Constant.ROUTE_PREFIX + userId);

        if (value == null){
            throw new IMException(StatusEnum.OFF_LINE) ;
        }

        String[] server = value.split(":");
        IMServerResVO imServerResVO = new IMServerResVO(server[0], Integer.parseInt(server[1]), Integer.parseInt(server[2]));
        return imServerResVO;
    }

    @Override
    public void pushMsg(String url, long sendUserId, ChatReqVO groupReqVO) throws Exception {
        IMUserInfo imUserInfo = userInfoCacheService.loadUserInfoByUserId(sendUserId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", imUserInfo.getUserName() + ":" + groupReqVO.getMsg());
        jsonObject.put("userId", groupReqVO.getUserId());
        RequestBody requestBody = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        try {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
        }finally {
            response.body().close();
        }
    }

    @Override
    public void offLine(Long userId) throws Exception {

        //删除路由
        redisTemplate.delete(Constant.ROUTE_PREFIX + userId) ;

        //删除登录状态
        userInfoCacheService.removeLoginStatus(userId);
    }
}
