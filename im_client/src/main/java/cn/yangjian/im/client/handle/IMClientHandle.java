package cn.yangjian.im.client.handle;

import cn.yangjian.im.client.service.EchoService;
import cn.yangjian.im.client.service.ShutDownMsg;
import cn.yangjian.im.client.service.impl.EchoServiceImpl;
import cn.yangjian.im.client.thread.ReConnectJob;
import cn.yangjian.im.client.util.SpringBeanFactory;
import cn.yangjian.im.common.constant.Constants;
import cn.yangjian.im.common.protocol.IMRequestProto;
import cn.yangjian.im.common.protocol.IMResponseProto;
import cn.yangjian.im.common.util.NettyAttrUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
//标注一个channel handler可以被多个channel安全地共享。
//因为一个ChannelHandler可以从属于多个ChannelPipeline，所以它也可以绑定到多个ChannelHandlerContext实例。
public class IMClientHandle extends SimpleChannelInboundHandler<IMResponseProto.IMResProtocol> {

    private final static Logger LOGGER = LoggerFactory.getLogger(IMClientHandle.class);

    private MsgHandleCaller caller;
    //
    private EchoService echoService;

    private ScheduledExecutorService scheduledExecutorService;

    private ShutDownMsg shutDownMsg;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, IMResponseProto.IMResProtocol imResProtocol) throws Exception {
        if (echoService == null) {
            echoService = SpringBeanFactory.getBean(EchoServiceImpl.class);
        }


        //心跳更新时间
        if (imResProtocol.getType() == Constants.CommandType.PING) {
            //LOGGER.info("收到服务端心跳！！！");
            NettyAttrUtil.updateReaderTime(channelHandlerContext.channel(), System.currentTimeMillis());
        }

        if (imResProtocol.getType() != Constants.CommandType.PING) {
            //回调消息
            callBackMsg(imResProtocol.getResMsg());

            echoService.echo(imResProtocol.getResMsg());
        }
    }


    /**
     * 回调消息
     *
     * @param msg
     */
    private void callBackMsg(String msg) {
        ThreadPoolExecutor threadPoolExecutor = SpringBeanFactory.getBean("callBackThreadPool", ThreadPoolExecutor.class);
        threadPoolExecutor.execute(() -> {
            caller = SpringBeanFactory.getBean(MsgHandleCaller.class);
            caller.getMsgHandleListener().handle(msg);
        });

    }


//    https://blog.csdn.net/linuu/article/details/51509847

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;

            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                IMRequestProto.IMReqProtocol heartBeat = SpringBeanFactory.getBean("heartBeat",
                        IMRequestProto.IMReqProtocol.class);
                ctx.writeAndFlush(heartBeat).addListeners((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        LOGGER.error("IO error,close Channel");
                        future.channel().close();
                    }
                });
            }

        }

        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //客户端和服务端建立连接时调用
        LOGGER.info("im server connect success!");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        if (shutDownMsg == null) {
            shutDownMsg = SpringBeanFactory.getBean(ShutDownMsg.class);
        }

        //用户主动退出，不执行重连逻辑
        if (shutDownMsg.checkStatus()) {
            return;
        }

        if (scheduledExecutorService == null) {
            scheduledExecutorService = SpringBeanFactory.getBean("scheduledTask", ScheduledExecutorService.class);
        }
        LOGGER.info("客户端断开了，重新连接！");

//        客观原因断开连接，重新连接
        scheduledExecutorService.scheduleAtFixedRate(new ReConnectJob(ctx), 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //异常时断开连接
        cause.printStackTrace();
        ctx.close();
    }
}
