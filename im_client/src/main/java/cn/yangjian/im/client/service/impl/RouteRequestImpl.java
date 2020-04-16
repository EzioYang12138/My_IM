package cn.yangjian.im.client.service.impl;

import cn.yangjian.im.client.config.AppConfiguration;
import cn.yangjian.im.client.service.EchoService;
import cn.yangjian.im.client.service.RouteRequest;
import cn.yangjian.im.client.vo.req.GroupReqVO;
import cn.yangjian.im.client.vo.req.LoginReqVO;
import cn.yangjian.im.client.vo.req.P2PReqVO;
import cn.yangjian.im.client.vo.res.IMServerResVO;
import cn.yangjian.im.client.vo.res.OnlineUsersResVO;
import cn.yangjian.im.common.enums.StatusEnum;
import cn.yangjian.im.common.res.BaseResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class RouteRequestImpl implements RouteRequest {

    private final static Logger LOGGER = LoggerFactory.getLogger(RouteRequestImpl.class);

    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${im.group.route.request.url}")
    private String groupRouteRequestUrl;

    @Value("${im.p2p.route.request.url}")
    private String p2pRouteRequestUrl;

    @Value("${im.server.online.user.url}")
    private String onlineUserUrl;

    @Value("${im.server.route.request.url}")
    private String serverRouteLoginUrl;

    @Autowired
    private EchoService echoService;

    @Autowired
    private AppConfiguration appConfiguration;

    private final MediaType mediaType = MediaType.parse("application/json");

    JSONObject jsonObject = new JSONObject();
    RequestBody requestBody;

    @Override
    public void sendGroupMsg(GroupReqVO groupReqVO) throws Exception {
        jsonObject.put("msg", groupReqVO.getMsg());
        jsonObject.put("userId", groupReqVO.getUserId());
        requestBody = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url(groupRouteRequestUrl)
                .post(requestBody)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        try {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
        } finally {
            response.body().close();
        }
    }

    @Override
    public void sendP2PMsg(P2PReqVO p2PReqVO) throws Exception {
        jsonObject.put("msg", p2PReqVO.getMsg());
        jsonObject.put("userId", p2PReqVO.getUserId());
        jsonObject.put("receiveUserId", p2PReqVO.getReceiveUserId());
        requestBody = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url(p2pRouteRequestUrl)
                .post(requestBody)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        ResponseBody body = response.body();
        try {
            String json = body.string();
            BaseResponse baseResponse = JSON.parseObject(json, BaseResponse.class);

            //选择的账号不存在
            if (baseResponse.getCode().equals(StatusEnum.OFF_LINE.getCode())) {
                LOGGER.error(p2PReqVO.getReceiveUserId() + ":" + StatusEnum.OFF_LINE.getMessage());
            }

        } finally {
            body.close();
        }
    }

    @Override
    public List<OnlineUsersResVO.DataBodyBean> onlineUsers() throws Exception {

        requestBody = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url(onlineUserUrl)
                .post(requestBody)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }


        ResponseBody body = response.body();
        OnlineUsersResVO onlineUsersResVO;
        try {
            String json = body.string();
            onlineUsersResVO = JSON.parseObject(json, OnlineUsersResVO.class);

        } finally {
            body.close();
        }

        return onlineUsersResVO.getDataBody();
    }

    @Override
    public IMServerResVO.ServerInfo getIMServer(LoginReqVO loginReqVO) throws Exception {

        jsonObject.put("userId", loginReqVO.getUserId());
        jsonObject.put("userName", loginReqVO.getUserName());
        requestBody = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url(serverRouteLoginUrl)
                .post(requestBody)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
        IMServerResVO imServerResVO;
        ResponseBody body = response.body();
        try {
            String json = body.string();
            imServerResVO = JSON.parseObject(json, IMServerResVO.class);

            //重复失败
            if (!imServerResVO.getCode().equals(StatusEnum.SUCCESS.getCode())) {
                echoService.echo(imServerResVO.getMessage());
                System.exit(-1);
            }

        } finally {
            body.close();
        }

        return imServerResVO.getDataBody();
    }

    @Override
    public void offLine() {
        jsonObject.put("userId", appConfiguration.getUserId());
        jsonObject.put("msg", "offLine");
        RequestBody requestBody = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url(appConfiguration.getClearRouteUrl())
                .post(requestBody)
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            LOGGER.error("exception", e);
        } finally {
            response.body().close();
        }
    }
}
