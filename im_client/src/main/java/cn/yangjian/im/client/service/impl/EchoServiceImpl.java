package cn.yangjian.im.client.service.impl;

import cn.yangjian.im.client.config.AppConfiguration;
import cn.yangjian.im.client.service.EchoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class EchoServiceImpl implements EchoService {

    private static final String PREFIX = "$";

    @Autowired
    private AppConfiguration appConfiguration;

    @Override
    public void echo(String msg, Object... replace) {

        String date = LocalDate.now().toString() + " " + LocalTime.now().withNano(0).toString();

        msg = "[" + date + "] \033[31;4m" + appConfiguration.getUserName() + PREFIX + "\033[0m" + " " + msg;

        String log = print(msg, replace);

        System.out.println(log);
    }

    private String print(String msg, Object... place) {
        StringBuilder sb = new StringBuilder();
        int k = 0;
        for (int i = 0; i < place.length; i++) {

//            从第k个位置开始查找 str
            int index = msg.indexOf("{}", k);

            if (index == -1) {
                return msg;
            }

            if (index != 0) {
                sb.append(msg, k, index);
                sb.append(place[i]);

                if (place.length == 1) {
                    sb.append(msg, index + 2, msg.length());
                }

            } else {
                sb.append(place[i]);
                if (place.length == 1) {
                    sb.append(msg, index + 2, msg.length());
                }
            }

            k = index + 2;
        }
        if (sb.toString().equals("")) {
            return msg;
        } else {
            return sb.toString();
        }
    }
}
