package cn.yangjian.im.client.service.impl.command;

import cn.yangjian.im.client.client.IMClient;
import cn.yangjian.im.client.service.*;
import cn.yangjian.im.common.dataconstruct.RingBufferWheel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class ShutDownCommand implements InnerCommand {
    private final static Logger LOGGER = LoggerFactory.getLogger(ShutDownCommand.class);

    @Autowired
    private RouteRequest routeRequest;

    @Autowired
    private IMClient imClient;

    @Autowired
    private MsgLogger msgLogger;

    @Resource(name = "callBackThreadPool")
    private ThreadPoolExecutor executor;

    @Autowired
    private EchoService echoService;

    @Autowired
    private RingBufferWheel ringBufferWheel;

    @Autowired
    private ShutDownMsg shutDownMsg;

    @Override
    public void process(String msg) {
        echoService.echo("客户端 正在关闭");
        shutDownMsg.shutdown();
        routeRequest.offLine();
        msgLogger.stop();
        executor.shutdown();
        ringBufferWheel.stop(false);
        try {
            while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                echoService.echo("线程池正在关闭");
            }
            imClient.close();
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException", e);
        }
        echoService.echo("客户端关闭成功");
        System.exit(0);
    }
}
