package cn.yangjian.im.client.vo.res;

import lombok.Data;

import java.util.List;

@Data
public class OnlineUsersResVO {

    private String code;
    private String message;
    private Object reqNo;
    private List<DataBodyBean> dataBody;


    @Data
    public static class DataBodyBean {

        private long userId;
        private String userName;

    }
}
