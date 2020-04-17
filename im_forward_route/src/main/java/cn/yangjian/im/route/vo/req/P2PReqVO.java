package cn.yangjian.im.route.vo.req;

import cn.yangjian.im.common.req.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
public class P2PReqVO extends BaseRequest {

    @NotNull(message = "userId 不能为空")
    @Getter
    @Setter
    private Long userId;


    @NotNull(message = "userId 不能为空")
    @Getter
    @Setter
    private Long receiveUserId;


    @NotNull(message = "msg 不能为空")
    @Getter
    @Setter
    private String msg;

}
