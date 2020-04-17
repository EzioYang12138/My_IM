package cn.yangjian.im.common.exception;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class GenericException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    String errorCode;

    @Getter
    @Setter
    String errorMessage;

    public GenericException(String message) {
        super(message);
    }

}
