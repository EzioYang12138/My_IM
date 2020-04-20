package cn.yangjian.im.server.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class AppConfiguration {

    @Value("${app.zk.root}")
    private String zkRoot;

    @Value("${app.zk.addr}")
    private String zkAddr;

    @Value("${im.server.port}")
    private int imServerPort;

    @Value("${im.clear.route.request.url}")
    private String clearRouteUrl ;

    @Value("${im.heartbeat.time}")
    private long heartBeatTime ;

    @Value("${app.zk.connect.timeout}")
    private int zkConnectTimeout;

}
