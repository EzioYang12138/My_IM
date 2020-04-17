package cn.yangjian.im.route.kit;


import cn.yangjian.im.route.config.AppConfiguration;
import cn.yangjian.im.route.util.SpringBeanFactory;

public class ServerListListener implements Runnable {

    private final ZKit zkUtil;

    private final AppConfiguration appConfiguration ;


    public ServerListListener() {
        zkUtil = SpringBeanFactory.getBean(ZKit.class) ;
        appConfiguration = SpringBeanFactory.getBean(AppConfiguration.class) ;
    }

    @Override
    public void run() {
        //注册监听服务
        zkUtil.subscribeEvent(appConfiguration.getZkRoot());
    }
}
