package cn.yangjian.im.client.service;

import cn.yangjian.im.client.vo.req.GroupReqVO;
import cn.yangjian.im.client.vo.req.P2PReqVO;

public interface MsgHandle {

    //校验消息  不能为空，后续可以加上一些敏感词
    boolean checkMsg(String msg);

    //是否执行内部命令
    boolean innerCommand(String msg);

    //统一的发送接口，包含了 groupChat p2pChat
    void sendMsg(String msg);

    //    群聊消息 其中的 userId 为发送者的 userID
    void groupChat(GroupReqVO groupReqVO) throws Exception;

    //    私聊请求
    void p2pChat(P2PReqVO p2PReqVO) throws Exception;

    //    关闭系统
    void shutdown();
}
