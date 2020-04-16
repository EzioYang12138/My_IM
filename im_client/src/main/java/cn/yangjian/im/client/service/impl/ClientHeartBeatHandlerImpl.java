package cn.yangjian.im.client.service.impl;

import cn.yangjian.im.client.client.IMClient;
import cn.yangjian.im.common.kit.HeartBeatHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientHeartBeatHandlerImpl implements HeartBeatHandler {

    @Autowired
    private IMClient imClient;

    @Override
    public void process(ChannelHandlerContext ctx) throws Exception {

        //重连
        imClient.reconnect();

    }


}
