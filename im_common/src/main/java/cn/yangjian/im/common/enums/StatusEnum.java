package cn.yangjian.im.common.enums;

import lombok.Getter;

public enum StatusEnum {

    /**
     * 成功
     */
    SUCCESS("9000", "成功"),


    /**
     * 重复登录
     */
    REPEAT_LOGIN("5000", "账号重复登录，请退出一个账号！"),

    /**
     * 账号不在线
     */
    OFF_LINE("7000", "你选择的账号不在线，请重新选择！"),

    /**
     * 登录信息不匹配
     */
    ACCOUNT_NOT_MATCH("9100", "登录信息不匹配！");


    /**
     * 枚举值码
     */
    @Getter
    private final String code;

    /**
     * 枚举描述
     */
    @Getter
    private final String message;

    /**
     * 构建一个 StatusEnum 。
     *
     * @param code    枚举值码。
     * @param message 枚举描述。
     */
    private StatusEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
    }
