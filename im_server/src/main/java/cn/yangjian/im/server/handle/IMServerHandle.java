package cn.yangjian.im.server.handle;

import cn.yangjian.im.common.constant.Constants;
import cn.yangjian.im.common.exception.IMException;
import cn.yangjian.im.common.kit.HeartBeatHandler;
import cn.yangjian.im.common.pojo.IMUserInfo;
import cn.yangjian.im.common.protocol.IMRequestProto;
import cn.yangjian.im.common.util.NettyAttrUtil;
import cn.yangjian.im.server.config.AppConfiguration;
import cn.yangjian.im.server.kit.ServerHeartBeatHandlerImpl;
import cn.yangjian.im.server.util.SessionSocketHolder;
import cn.yangjian.im.server.util.SpringBeanFactory;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@ChannelHandler.Sharable
public class IMServerHandle extends SimpleChannelInboundHandler<IMRequestProto.IMReqProtocol> {

    private final static Logger LOGGER = LoggerFactory.getLogger(IMServerHandle.class);

    private final MediaType mediaType = MediaType.parse("application/json");

    /**
     * 取消绑定
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //可能出现业务判断离线后再次触发 channelInactive
        IMUserInfo userInfo = SessionSocketHolder.getUserId((NioSocketChannel) ctx.channel());
        if (userInfo != null){
            LOGGER.warn("[{}]触发 channelInactive 掉线!",userInfo.getUserName());
            userOffLine(userInfo, (NioSocketChannel) ctx.channel());
            ctx.channel().close();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {

                LOGGER.info("定时检测客户端端是否存活");

                HeartBeatHandler heartBeatHandler = SpringBeanFactory.getBean(ServerHeartBeatHandlerImpl.class) ;
                heartBeatHandler.process(ctx) ;
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 用户下线
     * @param userInfo
     * @param channel
     * @throws IOException
     */
    private void userOffLine(IMUserInfo userInfo, NioSocketChannel channel) throws IOException {
        LOGGER.info("用户[{}]下线", userInfo.getUserName());
        SessionSocketHolder.remove(channel);
        SessionSocketHolder.removeSession(userInfo.getUserId());

        //清除路由关系
        clearRouteInfo(userInfo);
    }

    /**
     * 下线，清除路由关系
     *
     * @param userInfo
     * @throws IOException
     */
    private void clearRouteInfo(IMUserInfo userInfo) throws IOException {
        OkHttpClient okHttpClient = SpringBeanFactory.getBean(OkHttpClient.class);
        AppConfiguration configuration = SpringBeanFactory.getBean(AppConfiguration.class);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", userInfo.getUserId());
        jsonObject.put("msg", "offLine");
        RequestBody requestBody = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url(configuration.getClearRouteUrl())
                .post(requestBody)
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
        } finally {
            response.body().close();
        }
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMRequestProto.IMReqProtocol msg) throws Exception {
        LOGGER.info("收到msg={}", msg.toString());

        if (msg.getType() == Constants.CommandType.LOGIN) {
            //保存客户端与 Channel 之间的关系
            SessionSocketHolder.put(msg.getRequestId(), (NioSocketChannel) ctx.channel());
            SessionSocketHolder.saveSession(msg.getRequestId(), msg.getReqMsg());
            LOGGER.info("客户端[{}]上线成功", msg.getReqMsg());
        }

        //心跳更新时间
        if (msg.getType() == Constants.CommandType.PING){
            NettyAttrUtil.updateReaderTime(ctx.channel(),System.currentTimeMillis());
            //向客户端响应 pong 消息
            IMRequestProto.IMReqProtocol heartBeat = SpringBeanFactory.getBean("heartBeat",
                    IMRequestProto.IMReqProtocol.class);
            ctx.writeAndFlush(heartBeat).addListeners((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    LOGGER.error("IO error,close Channel");
                    future.channel().close();
                }
            }) ;
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (IMException.isResetByPeer(cause.getMessage())) {
            return;
        }

        LOGGER.error(cause.getMessage(), cause);

    }

}
