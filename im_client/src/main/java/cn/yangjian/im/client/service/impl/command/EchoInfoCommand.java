package cn.yangjian.im.client.service.impl.command;

import cn.yangjian.im.client.service.EchoService;
import cn.yangjian.im.client.service.InnerCommand;
import cn.yangjian.im.client.service.impl.ClientInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EchoInfoCommand implements InnerCommand {

    @Autowired
    private ClientInfo clientInfo;

    @Autowired
    private EchoService echoService ;

    @Override
    public void process(String msg) {
        echoService.echo("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        echoService.echo("client info={}", clientInfo.getUserName());
        echoService.echo("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }
}
