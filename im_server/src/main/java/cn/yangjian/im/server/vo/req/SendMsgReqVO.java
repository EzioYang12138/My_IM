package cn.yangjian.im.server.vo.req;

import cn.yangjian.im.common.req.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SendMsgReqVO extends BaseRequest {

    @NotNull(message = "msg 不能为空")
    private String msg;

    @NotNull(message = "userId 不能为空")
    private Long userId;

}
