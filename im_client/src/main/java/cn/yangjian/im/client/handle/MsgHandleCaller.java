package cn.yangjian.im.client.handle;

import cn.yangjian.im.client.service.CustomMsgHandleListener;
import lombok.AllArgsConstructor;
import lombok.Data;

//消息回调的bean
@AllArgsConstructor
@Data
public class MsgHandleCaller {

    /**
     * 回调接口
     */
    private CustomMsgHandleListener msgHandleListener ;

}
