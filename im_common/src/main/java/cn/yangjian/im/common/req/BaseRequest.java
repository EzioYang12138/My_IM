package cn.yangjian.im.common.req;

import lombok.Data;

@Data
public class BaseRequest {

    private String reqNo;

    private int timeStamp;

    public BaseRequest() {
        this.setTimeStamp((int)(System.currentTimeMillis() / 1000));
    }

}
