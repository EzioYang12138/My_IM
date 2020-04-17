package cn.yangjian.im.common.res;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class BaseResponse<T> implements Serializable {
    private String code;

    private String message;

//    请求号
    private String reqNo;

    private T dataBody;

}

