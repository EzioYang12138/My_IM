package cn.yangjian.im.client.scanner;

import cn.yangjian.im.client.service.MsgHandle;
import cn.yangjian.im.client.service.MsgLogger;
import cn.yangjian.im.client.util.SpringBeanFactory;

import java.util.Scanner;

public class Scan implements Runnable {

    private final MsgHandle msgHandle;

    private final MsgLogger msgLogger;

    public Scan() {
        this.msgHandle = SpringBeanFactory.getBean(MsgHandle.class);
        this.msgLogger = SpringBeanFactory.getBean(MsgLogger.class);
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            String msg = sc.nextLine();

            //检查消息
            if (msgHandle.checkMsg(msg)) {
                continue;
            }
            //系统内置命令
            if (msgHandle.innerCommand(msg)) {
                continue;
            }

            //真正的发送消息
            msgHandle.sendMsg(msg);

            //写入聊天记录
            msgLogger.log(msg);

        }
    }
}
