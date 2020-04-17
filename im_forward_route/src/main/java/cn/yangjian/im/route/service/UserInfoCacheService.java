package cn.yangjian.im.route.service;

import cn.yangjian.im.common.pojo.IMUserInfo;

import java.util.Set;

public interface UserInfoCacheService {

    //    通过 userID 获取用户信息
    IMUserInfo loadUserInfoByUserId(Long userId);

    //    保存和检查用户登录情况
    boolean saveAndCheckUserLoginStatus(Long userId) throws Exception;

    //    清除用户的登录状态
    void removeLoginStatus(Long userId) throws Exception;

    //    获取所有在线用户
    Set<IMUserInfo> onlineUser();
}
