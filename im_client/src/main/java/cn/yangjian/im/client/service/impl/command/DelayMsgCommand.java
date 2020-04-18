package cn.yangjian.im.client.service.impl.command;

import cn.yangjian.im.client.service.EchoService;
import cn.yangjian.im.client.service.InnerCommand;
import cn.yangjian.im.client.service.MsgHandle;
import cn.yangjian.im.common.dataconstruct.RingBufferWheel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DelayMsgCommand implements InnerCommand {

    @Autowired
    private EchoService echoService;

    @Autowired
    private MsgHandle msgHandle;

    @Autowired
    private RingBufferWheel ringBufferWheel;


    @Override
    public void process(String msg) {
        if (msg.split(" ").length <= 2) {
            echoService.echo("输入形式错误,正确格式为 :delay [msg] [delayTime]");
            return;
        }

        String message = msg.split(" ")[1];
        int delayTime = Integer.parseInt(msg.split(" ")[2]);

        RingBufferWheel.Task task = new DelayMsgJob(message);
        task.setKey(delayTime);
        ringBufferWheel.addTask(task);
        echoService.echo(msg);
    }

    private class DelayMsgJob extends RingBufferWheel.Task {

        private String msg;

        public DelayMsgJob(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            msgHandle.sendMsg(msg);
        }
    }
}
