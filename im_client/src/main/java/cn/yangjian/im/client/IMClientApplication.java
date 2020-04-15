package cn.yangjian.im.client;

import cn.yangjian.im.client.scanner.Scan;
import cn.yangjian.im.client.service.impl.ClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IMClientApplication implements CommandLineRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(IMClientApplication.class);

    @Autowired
    private ClientInfo clientInfo ;

    public static void main(String[] args) {
        SpringApplication.run(IMClientApplication.class, args);
        LOGGER.info("启动 Client 服务成功");
    }

    @Override
    public void run(String... args) throws Exception {
        Scan scan = new Scan() ;
        Thread thread = new Thread(scan);
        thread.setName("scan-thread");
        thread.start();
        clientInfo.saveStartDate();
    }
}
