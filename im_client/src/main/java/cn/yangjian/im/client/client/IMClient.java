package cn.yangjian.im.client.client;

import cn.yangjian.im.client.config.AppConfiguration;
import cn.yangjian.im.client.init.IMClientHandleInitializer;
import cn.yangjian.im.client.service.EchoService;
import cn.yangjian.im.client.service.MsgHandle;
import cn.yangjian.im.client.service.RouteRequest;
import cn.yangjian.im.client.service.impl.ClientInfo;
import cn.yangjian.im.client.vo.req.LoginReqVO;
import cn.yangjian.im.client.vo.res.IMServerResVO;
import cn.yangjian.im.common.constant.Constants;
import cn.yangjian.im.common.protocol.IMRequestProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class IMClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(IMClient.class);

    //    客户端需要一个时间循环组
    private final EventLoopGroup group = new NioEventLoopGroup(0, new DefaultThreadFactory("im-work"));

    @Value("${im.user.id}")
    private long userId;

    @Value("${im.user.userName}")
    private String userName;

    private SocketChannel channel;

    @Autowired
    private EchoService echoService;

    @Autowired
    private RouteRequest routeRequest;

    @Autowired
    private AppConfiguration configuration;

    @Autowired
    private MsgHandle msgHandle;

    @Autowired
    private ClientInfo clientInfo;

    //    重试次数
    private int errorCount;

    //   @PostConstruct 用来修饰一个非静态的void（）方法
//    被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，
//    并且只会被服务器执行一次。PostConstruct在构造函数之后执行，init（）方法之前执行。
    @PostConstruct
    public void start() throws Exception {

        //登录 + 获取可以使用的服务器 ip+port
        IMServerResVO.ServerInfo imServer = userLogin();

        //启动客户端
        startClient(imServer);

        //向服务端注册
        loginIMServer();
    }

    //    登录+路由服务器
    private IMServerResVO.ServerInfo userLogin() {
        LoginReqVO loginReqVO = new LoginReqVO(userId, userName);
        IMServerResVO.ServerInfo imServer = null;
        try {
            imServer = routeRequest.getIMServer(loginReqVO);

            //保存系统信息
            clientInfo.saveServiceInfo(imServer.getIp() + ":" + imServer.getImServerPort())
                    .saveClientInfo(userId, userName);

            LOGGER.info("imServer=[{}]", imServer.toString());
        } catch (Exception e) {
            errorCount++;

            if (errorCount >= configuration.getErrorCount()) {
                LOGGER.error("重连次数达到上限[{}]次", errorCount);
                msgHandle.shutdown();
            }
            LOGGER.error("login fail", e);
        }
        return imServer;
    }

    //    启动客户端
    private void startClient(IMServerResVO.ServerInfo imServer) {
//        创建客户端启动对象bootstrap
        Bootstrap bootstrap = new Bootstrap();
//        设置线程组
        bootstrap.group(group)
//                设置客户端通道的实现类（反射）
                .channel(NioSocketChannel.class)

                .handler(new IMClientHandleInitializer())
        ;

//        netty的异步模型
        ChannelFuture future = null;
        try {
//            启动客户端去连接服务器端
            future = bootstrap.connect(imServer.getIp(), imServer.getImServerPort()).sync();
        } catch (InterruptedException e) {
            errorCount++;

            if (errorCount >= configuration.getErrorCount()) {
                LOGGER.error("连接失败次数达到上限[{}]次", errorCount);
                msgHandle.shutdown();
            }
            LOGGER.error("连接失败", e);
        }
        if (future.isSuccess()) {
            echoService.echo("start im client success!");
            LOGGER.info("启动 im client 成功");
        }
        channel = (SocketChannel) future.channel();//返回当前正在进行 IO 操作的通道
    }

    /**
     * 向服务器注册
     */
    private void loginIMServer() {
        IMRequestProto.IMReqProtocol login = IMRequestProto.IMReqProtocol.newBuilder()
                .setRequestId(userId)
                .setReqMsg(userName)
                .setType(Constants.CommandType.LOGIN)
                .build();
        ChannelFuture future = channel.writeAndFlush(login);
        future.addListener((ChannelFutureListener) channelFuture -> echoService.echo("registry im server success!"));
    }

    public void reconnect() throws Exception {
        if (channel != null && channel.isActive()) {
            return;
        }
        //首先清除路由信息，下线
        routeRequest.offLine();

        LOGGER.info("reconnect....");
        start();
        LOGGER.info("reconnect success");
    }

    //    关闭
    public void close() throws InterruptedException {
        if (channel != null) {
            channel.close();
        }
    }
}
