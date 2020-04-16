package cn.yangjian.im.client.vo.req;

import cn.yangjian.im.common.req.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;


@AllArgsConstructor
public class GroupReqVO extends BaseRequest {

    @Getter
    @Setter
    @NotNull(message = "userId 不能为空")
    private Long userId ;

    @Getter
    @Setter
    @NotNull(message = "msg 不能为空")
    private String msg ;

}
