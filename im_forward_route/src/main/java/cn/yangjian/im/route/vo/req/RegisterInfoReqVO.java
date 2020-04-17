package cn.yangjian.im.route.vo.req;

import cn.yangjian.im.common.req.BaseRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

public class RegisterInfoReqVO extends BaseRequest {

    @NotNull(message = "用户名不能为空")
    @Getter
    @Setter
    private String userName;

}
