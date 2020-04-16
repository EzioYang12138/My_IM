package cn.yangjian.im.client.service.impl;

import cn.yangjian.im.client.service.CustomMsgHandleListener;
import cn.yangjian.im.client.service.MsgLogger;
import cn.yangjian.im.client.util.SpringBeanFactory;

//消息回调实现类
public class MsgCallBackListener implements CustomMsgHandleListener {


    private final MsgLogger msgLogger ;

    public MsgCallBackListener() {
        this.msgLogger = SpringBeanFactory.getBean(MsgLogger.class) ;
    }

    @Override
    public void handle(String msg) {
        msgLogger.log(msg) ;
    }
}
