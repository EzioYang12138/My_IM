package cn.yangjian.im.client.service.impl;

import cn.yangjian.im.client.client.IMClient;
import cn.yangjian.im.client.config.AppConfiguration;
import cn.yangjian.im.client.service.*;
import cn.yangjian.im.client.vo.req.GroupReqVO;
import cn.yangjian.im.client.vo.req.P2PReqVO;
import cn.yangjian.im.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class MsgHandler implements MsgHandle {

    private final static Logger LOGGER = LoggerFactory.getLogger(MsgHandler.class);

    @Autowired
    private RouteRequest routeRequest;

    @Autowired
    private InnerCommandContext innerCommandContext;

    @Autowired
    private AppConfiguration configuration;

    @Autowired
    private MsgLogger msgLogger;

    @Resource(name = "callBackThreadPool")
    private ThreadPoolExecutor executor;

    @Autowired
    private IMClient imClient;

    @Override
    public boolean checkMsg(String msg) {
        if (StringUtil.isEmpty(msg)) {
            LOGGER.warn("不能发送空消息！");
            return true;
        }
        return false;
    }

    @Override
    public boolean innerCommand(String msg) {
        if (msg.startsWith(":")) {
            InnerCommand instance = innerCommandContext.getInstance(msg);
            instance.process(msg);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void sendMsg(String msg) {
        String[] totalMsg = msg.split(";;");
        if (totalMsg.length > 1) {
            //私聊
            P2PReqVO p2PReqVO = new P2PReqVO();
            p2PReqVO.setUserId(configuration.getUserId());
            p2PReqVO.setReceiveUserId(Long.parseLong(totalMsg[0]));
            p2PReqVO.setMsg(totalMsg[1]);
            try {
                p2pChat(p2PReqVO);
            } catch (Exception e) {
                LOGGER.error("Exception", e);
            }

        } else {
            //群聊
            GroupReqVO groupReqVO = new GroupReqVO(configuration.getUserId(), msg);
            try {
                groupChat(groupReqVO);
            } catch (Exception e) {
                LOGGER.error("Exception", e);
            }
        }
    }

    @Override
    public void groupChat(GroupReqVO groupReqVO) throws Exception {
        routeRequest.sendGroupMsg(groupReqVO);
    }

    @Override
    public void p2pChat(P2PReqVO p2PReqVO) throws Exception {
        routeRequest.sendP2PMsg(p2PReqVO);
    }

    @Override
    public void shutdown() {
        LOGGER.info("系统关闭中。。。。");
        routeRequest.offLine();
        msgLogger.stop();
        executor.shutdown();
        try {
            while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                LOGGER.info("线程池关闭中。。。。");
            }
            imClient.close();
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException", e);
        }
        System.exit(0);
    }
}
