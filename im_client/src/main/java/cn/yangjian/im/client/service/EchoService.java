package cn.yangjian.im.client.service;

public interface EchoService {

//    在终端打印信息

//    可以传入多个参数
    void echo(String msg , Object... replace);
}
