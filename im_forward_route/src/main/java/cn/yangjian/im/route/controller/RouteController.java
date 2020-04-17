package cn.yangjian.im.route.controller;

import cn.yangjian.im.common.enums.StatusEnum;
import cn.yangjian.im.common.exception.IMException;
import cn.yangjian.im.common.pojo.IMUserInfo;
import cn.yangjian.im.common.res.BaseResponse;
import cn.yangjian.im.common.res.NullBody;
import cn.yangjian.im.common.routealgorithm.RouteHandle;
import cn.yangjian.im.route.cache.ServerCache;
import cn.yangjian.im.route.service.AccountService;
import cn.yangjian.im.route.service.UserInfoCacheService;
import cn.yangjian.im.route.vo.req.ChatReqVO;
import cn.yangjian.im.route.vo.req.LoginReqVO;
import cn.yangjian.im.route.vo.req.P2PReqVO;
import cn.yangjian.im.route.vo.req.RegisterInfoReqVO;
import cn.yangjian.im.route.vo.res.IMServerResVO;
import cn.yangjian.im.route.vo.res.RegisterInfoResVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/")
public class RouteController {
    private final static Logger LOGGER = LoggerFactory.getLogger(RouteController.class);

    @Autowired
    private ServerCache serverCache;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserInfoCacheService userInfoCacheService;


    @Autowired
    private RouteHandle routeHandle;

    //    群聊
    @RequestMapping(value = "groupRoute", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse<NullBody> groupRoute(@RequestBody ChatReqVO groupReqVO) throws Exception {
        BaseResponse<NullBody> res = new BaseResponse();

        LOGGER.info("msg=[{}]", groupReqVO.toString());

        //获取所有的推送列表
        Map<Long, IMServerResVO> serverResVOMap = accountService.loadRouteRelated();
        for (Map.Entry<Long, IMServerResVO> imServerResVOEntry : serverResVOMap.entrySet()) {
            Long userId = imServerResVOEntry.getKey();
            IMServerResVO value = imServerResVOEntry.getValue();
            if (userId.equals(groupReqVO.getUserId())) {
                //过滤掉自己
                IMUserInfo imUserInfo = userInfoCacheService.loadUserInfoByUserId(groupReqVO.getUserId());
                LOGGER.warn("自己不能给自己发信息，所以过滤掉了发送者 userId={}", imUserInfo.toString());
                continue;
            }

            //推送消息
            String url = "http://" + value.getIp() + ":" + value.getHttpPort() + "/sendMsg";
            ChatReqVO chatVO = new ChatReqVO(userId, groupReqVO.getMsg());

            accountService.pushMsg(url, groupReqVO.getUserId(), chatVO);

        }

        res.setCode(StatusEnum.SUCCESS.getCode());
        res.setMessage(StatusEnum.SUCCESS.getMessage());
        return res;
    }

    //    私聊
    @RequestMapping(value = "p2pRoute", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse<NullBody> p2pRoute(@RequestBody P2PReqVO p2pRequest) throws Exception {
        BaseResponse<NullBody> res = new BaseResponse();

        try {
            //获取接收消息用户的路由信息
            IMServerResVO imServerResVO = accountService.loadRouteRelatedByUserId(p2pRequest.getReceiveUserId());
            //推送消息
            String url = "http://" + imServerResVO.getIp() + ":" + imServerResVO.getHttpPort() + "/sendMsg";

            //p2pRequest.getReceiveUserId()==>消息接收者的 userID
            ChatReqVO chatVO = new ChatReqVO(p2pRequest.getReceiveUserId(), p2pRequest.getMsg());
            accountService.pushMsg(url, p2pRequest.getUserId(), chatVO);

            res.setCode(StatusEnum.SUCCESS.getCode());
            res.setMessage(StatusEnum.SUCCESS.getMessage());

        } catch (IMException e) {
            res.setCode(e.getErrorCode());
            res.setMessage(e.getErrorMessage());
        }
        return res;
    }

    //    客户端下线
    @RequestMapping(value = "offLine", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse<NullBody> offLine(@RequestBody ChatReqVO groupReqVO) throws Exception {
        BaseResponse<NullBody> res = new BaseResponse();

        IMUserInfo imUserInfo = userInfoCacheService.loadUserInfoByUserId(groupReqVO.getUserId());

        LOGGER.info("下线用户[{}]", imUserInfo.toString());
        accountService.offLine(groupReqVO.getUserId());

        res.setCode(StatusEnum.SUCCESS.getCode());
        res.setMessage(StatusEnum.SUCCESS.getMessage());
        return res;
    }

    //    获取一台 CIM server 登录并获取服务器
    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse<IMServerResVO> login(@RequestBody LoginReqVO loginReqVO) throws Exception {
        BaseResponse<IMServerResVO> res = new BaseResponse<>();

        //登录校验
        StatusEnum status = accountService.login(loginReqVO);
        if (status == StatusEnum.SUCCESS) {

            String server = routeHandle.routeServer(serverCache.getAll(), String.valueOf(loginReqVO.getUserId()));
            String[] serverInfo = server.split(":");
            IMServerResVO vo = new IMServerResVO(serverInfo[0], Integer.parseInt(serverInfo[1]), Integer.parseInt(serverInfo[2]));

            //保存路由信息
            accountService.saveRouteInfo(loginReqVO, server);

            res.setDataBody(vo);

        }
        res.setCode(status.getCode());
        res.setMessage(status.getMessage());

        return res;
    }

    //    注册账号
    @RequestMapping(value = "registerAccount", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse<RegisterInfoResVO> registerAccount(@RequestBody RegisterInfoReqVO registerInfoReqVO) throws Exception {
        BaseResponse<RegisterInfoResVO> res = new BaseResponse();

        long userId = System.currentTimeMillis();
        RegisterInfoResVO info = new RegisterInfoResVO(userId, registerInfoReqVO.getUserName());
        info = accountService.register(info);

        res.setDataBody(info);
        res.setCode(StatusEnum.SUCCESS.getCode());
        res.setMessage(StatusEnum.SUCCESS.getMessage());
        return res;
    }

    //    获取所有在线用户
    @RequestMapping(value = "onlineUser", method = RequestMethod.POST)
    @ResponseBody
    public BaseResponse<Set<IMUserInfo>> onlineUser() throws Exception {
        BaseResponse<Set<IMUserInfo>> res = new BaseResponse();

        Set<IMUserInfo> imUserInfos = userInfoCacheService.onlineUser();
        res.setDataBody(imUserInfos);
        res.setCode(StatusEnum.SUCCESS.getCode());
        res.setMessage(StatusEnum.SUCCESS.getMessage());
        return res;
    }
}
