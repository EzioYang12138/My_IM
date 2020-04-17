package cn.yangjian.im.route.service.impl;

import cn.yangjian.im.common.pojo.IMUserInfo;
import cn.yangjian.im.route.constant.Constant;
import cn.yangjian.im.route.service.UserInfoCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserInfoCacheServiceImpl implements UserInfoCacheService {

    //    本地缓存
    private final static Map<Long, IMUserInfo> USER_INFO_MAP = new ConcurrentHashMap<>(64);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public IMUserInfo loadUserInfoByUserId(Long userId) {

        //优先从本地缓存获取
        IMUserInfo cimUserInfo = USER_INFO_MAP.get(userId);
        if (cimUserInfo != null) {
            return cimUserInfo;
        }

        //load redis
        String sendUserName = redisTemplate.opsForValue().get(Constant.ACCOUNT_PREFIX + userId);
        if (sendUserName != null) {
            cimUserInfo = new IMUserInfo(userId, sendUserName);
            USER_INFO_MAP.put(userId, cimUserInfo);
        }

        return cimUserInfo;
    }

    @Override
    public boolean saveAndCheckUserLoginStatus(Long userId) throws Exception {

        Long add = redisTemplate.opsForSet().add(Constant.LOGIN_STATUS_PREFIX, userId.toString());
        if (add == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void removeLoginStatus(Long userId) throws Exception {
        redisTemplate.opsForSet().remove(Constant.LOGIN_STATUS_PREFIX, userId.toString());
    }

    @Override
    public Set<IMUserInfo> onlineUser() {
        Set<IMUserInfo> set = null;
        Set<String> members = redisTemplate.opsForSet().members(Constant.LOGIN_STATUS_PREFIX);
        for (String member : members) {
            if (set == null) {
                set = new HashSet<>(64);
            }
            IMUserInfo imUserInfo = loadUserInfoByUserId(Long.valueOf(member));
            set.add(imUserInfo);
        }

        return set;
    }
}
