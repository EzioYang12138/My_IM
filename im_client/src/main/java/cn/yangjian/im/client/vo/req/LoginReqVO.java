package cn.yangjian.im.client.vo.req;

import cn.yangjian.im.common.req.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class LoginReqVO extends BaseRequest {

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private String userName;
}
