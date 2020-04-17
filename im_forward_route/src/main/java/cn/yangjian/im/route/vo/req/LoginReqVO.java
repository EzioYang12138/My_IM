package cn.yangjian.im.route.vo.req;

import cn.yangjian.im.common.req.BaseRequest;
import lombok.Getter;
import lombok.Setter;

public class LoginReqVO extends BaseRequest {

    @Getter
    @Setter
    private Long userId ;

    @Getter
    @Setter
    private String userName ;
}
