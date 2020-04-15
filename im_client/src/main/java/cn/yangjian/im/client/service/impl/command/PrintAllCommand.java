package cn.yangjian.im.client.service.impl.command;

import cn.yangjian.im.client.service.EchoService;
import cn.yangjian.im.client.service.InnerCommand;
import cn.yangjian.im.common.enums.SystemCommandEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PrintAllCommand implements InnerCommand {

    @Autowired
    private EchoService echoService;

    @Override
    public void process(String msg) {
        Map<String, String> allStatusCode = SystemCommandEnum.getAllStatusCode();
//        [2020-04-13 19:10:44] moyu$ ====================================
        echoService.echo("====================================");
        for (Map.Entry<String, String> stringStringEntry : allStatusCode.entrySet()) {
            String key = stringStringEntry.getKey();
            String value = stringStringEntry.getValue();
            echoService.echo(key + "----->" + value);
        }
        echoService.echo("====================================");
    }
}
