package cn.yangjian.im.common.exception;

import cn.yangjian.im.common.enums.StatusEnum;

public class IMException extends GenericException {


    public IMException(StatusEnum statusEnum) {
        super(statusEnum.getMessage());
        this.errorMessage = statusEnum.getMessage();
        this.errorCode = statusEnum.getCode();
    }

    public static boolean isResetByPeer(String msg) {
        if ("Connection reset by peer".equals(msg)) {
            return true;
        }
        return false;
    }

}
