package cn.yangjian.im.client.service.impl;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Data
public class ClientInfo {

    private String userName;
    private long userId;
    private String serviceInfo;
    private Date startDate;

    public void saveClientInfo(long userId, String userName){
        this.setUserId(userId);
        this.setUserName(userName);
    }

    public ClientInfo saveServiceInfo(String serviceInfo) {
        this.setServiceInfo(serviceInfo);
        return this;
    }

    public void saveStartDate(){
        this.setStartDate(new Date());
    }
}
