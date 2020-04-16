package cn.yangjian.im.client.service.impl.command;

import cn.yangjian.im.client.service.EchoService;
import cn.yangjian.im.client.service.InnerCommand;
import cn.yangjian.im.client.service.RouteRequest;
import cn.yangjian.im.client.vo.res.OnlineUsersResVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PrintOnlineUsersCommand implements InnerCommand {
    private final static Logger LOGGER = LoggerFactory.getLogger(PrintOnlineUsersCommand.class);

    @Autowired
    private RouteRequest routeRequest;

    @Autowired
    private EchoService echoService;

    @Override
    public void process(String msg) {
        try {
            List<OnlineUsersResVO.DataBodyBean> onlineUsers = routeRequest.onlineUsers();

            echoService.echo("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            for (OnlineUsersResVO.DataBodyBean onlineUser : onlineUsers) {
                echoService.echo("userId={}=====userName={}", onlineUser.getUserId(), onlineUser.getUserName());
            }
            echoService.echo("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
    }
}
