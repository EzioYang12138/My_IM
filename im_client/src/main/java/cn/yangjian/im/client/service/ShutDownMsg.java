package cn.yangjian.im.client.service;

import org.springframework.stereotype.Component;

@Component
public class ShutDownMsg {
    private boolean isStopByUser;

    /**
     * 置为用户主动退出状态
     */
    public void shutdown() {
        isStopByUser = true;
    }

    public boolean checkStatus() {
        return isStopByUser;
    }
}
