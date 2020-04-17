package cn.yangjian.im.route.service;

import cn.yangjian.im.common.enums.StatusEnum;
import cn.yangjian.im.route.vo.req.ChatReqVO;
import cn.yangjian.im.route.vo.req.LoginReqVO;
import cn.yangjian.im.route.vo.res.IMServerResVO;
import cn.yangjian.im.route.vo.res.RegisterInfoResVO;

import java.util.Map;

public interface AccountService {

    //    注册用户
    RegisterInfoResVO register(RegisterInfoResVO info) throws Exception;

    //    登录服务
    StatusEnum login(LoginReqVO loginReqVO) throws Exception;

    //    保存路由信息
    void saveRouteInfo(LoginReqVO loginReqVO, String msg) throws Exception;

    //    加载所有用户的路由关系
    Map<Long, IMServerResVO> loadRouteRelated();

    //    获取某个用户的路由关系
    IMServerResVO loadRouteRelatedByUserId(Long userId);


    //    推送消息
    void pushMsg(String url, long sendUserId, ChatReqVO groupReqVO) throws Exception;

    //    用户下线
    void offLine(Long userId) throws Exception;
}
