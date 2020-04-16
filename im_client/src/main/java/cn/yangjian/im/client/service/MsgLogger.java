package cn.yangjian.im.client.service;

public interface MsgLogger {

    //    异步写入消息
    void log(String msg);

    //    通过key 关键字查询聊天记录
    String query(String key);

    //    停止写入
    void stop();
}
