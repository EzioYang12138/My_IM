package cn.yangjian.im.client.service;

import cn.yangjian.im.client.vo.req.GroupReqVO;
import cn.yangjian.im.client.vo.req.LoginReqVO;
import cn.yangjian.im.client.vo.req.P2PReqVO;
import cn.yangjian.im.client.vo.res.IMServerResVO;
import cn.yangjian.im.client.vo.res.OnlineUsersResVO;

import java.util.List;

public interface RouteRequest {

    //    群发消息
    void sendGroupMsg(GroupReqVO groupReqVO) throws Exception;

    //    私聊
    void sendP2PMsg(P2PReqVO p2PReqVO) throws Exception;

    //    获取所有在线用户
    List<OnlineUsersResVO.DataBodyBean> onlineUsers() throws Exception;

    //    获取服务器
    IMServerResVO.ServerInfo getIMServer(LoginReqVO loginReqVO) throws Exception;

    //    清除路由信息并下线
    void offLine();
}
