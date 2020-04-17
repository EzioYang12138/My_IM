package cn.yangjian.im.server.kit;

import cn.yangjian.im.server.config.AppConfiguration;
import cn.yangjian.im.server.util.SpringBeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryZK implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RegistryZK.class);

    private final ZKit zKit;

    private final AppConfiguration appConfiguration;

    private final String ip;
    private final int imServerPort;
    private final int httpPort;

    public RegistryZK(String ip, int imServerPort, int httpPort) {
        this.ip = ip;
        this.imServerPort = imServerPort;
        this.httpPort = httpPort;
        zKit = SpringBeanFactory.getBean(ZKit.class);
        appConfiguration = SpringBeanFactory.getBean(AppConfiguration.class);
    }

    @Override
    public void run() {

        //创建父节点
        zKit.createRootNode();

        //是否要将自己注册到 ZK
        if (appConfiguration.isZkSwitch()) {
            String path = appConfiguration.getZkRoot() + "/ip-" + ip + ":" + imServerPort + ":" + httpPort;
            zKit.createNode(path);
            logger.info("注册 zookeeper 成功，msg=[{}]", path);
        }


    }
}
