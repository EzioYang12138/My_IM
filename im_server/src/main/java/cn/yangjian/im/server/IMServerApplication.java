package cn.yangjian.im.server;

import cn.yangjian.im.server.config.AppConfiguration;
import cn.yangjian.im.server.kit.RegistryZK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;

@SpringBootApplication
public class IMServerApplication implements CommandLineRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(IMServerApplication.class);

    @Autowired
    private AppConfiguration appConfiguration;

    @Value("${server.port}")
    private int httpPort;

    public static void main(String[] args) {
        SpringApplication.run(IMServerApplication.class, args);
        LOGGER.info("启动 Server 成功");
    }


//	Spring Boot应用程序在启动后，会遍历CommandLineRunner接口的实例并运行它们的run方法。

    @Override
    public void run(String... args) throws Exception {
        //获得本机IP
        String addr = InetAddress.getLocalHost().getHostAddress();
        Thread thread = new Thread(new RegistryZK(addr, appConfiguration.getImServerPort(), httpPort));
        thread.setName("registry-zk");
        thread.start();
    }
}
