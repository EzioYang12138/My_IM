package cn.yangjian.im.client.vo.req;

import cn.yangjian.im.common.req.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;


@AllArgsConstructor
@NoArgsConstructor
public class P2PReqVO extends BaseRequest {

    @Getter
    @Setter
    @NotNull(message = "消息发送者的userId 不能为空")
    private Long userId ;

    @Getter
    @Setter
    @NotNull(message = "消息接收者的userId 不能为空")
    private Long receiveUserId ;

    @Getter
    @Setter
    @NotNull(message = "msg 不能为空")
    private String msg ;

}
