package cn.yangjian.im.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum SystemCommandEnum {

    ALL(":all       ", "获取所有命令", "PrintAllCommand"),
    ONLINE_USER(":olu       ", "获取所有在线用户", "PrintOnlineUsersCommand"),
    QUIT(":q!        ", "退出程序", "ShutDownCommand"),
    QUERY(":q         ", "【:q 关键字】查询聊天记录", "QueryHistoryCommand"),
    PREFIX(":pu        ", "模糊匹配用户", "PrefixSearchCommand"),
    INFO(":info      ", "获取客户端信息", "EchoInfoCommand"),
    DELAY_MSG(":delay     ", "delay message, :delay [msg] [delayTime]", "DelayMsgCommand");

    /**
     * 枚举值码
     */
    @Getter
    private final String commandType;

    /**
     * 枚举描述
     */
    @Getter
    private final String desc;

    /**
     * 实现类
     */
    @Getter
    private final String clazz;


    /**
     * 构建一个 。
     *
     * @param commandType 枚举值码。
     * @param desc        枚举描述。
     */
    SystemCommandEnum(String commandType, String desc, String clazz) {
        this.commandType = commandType;
        this.desc = desc;
        this.clazz = clazz;
    }


    /**
     * 获取全部枚举值码。
     *
     * @return 全部枚举值码。
     */
    public static Map<String, String> getAllStatusCode() {
        Map<String, String> map = new HashMap<>(16);
        for (SystemCommandEnum status : SystemCommandEnum.values()) {
            map.put(status.getCommandType(), status.getDesc());
        }
        return map;
    }

    public static Map<String, String> getAllClazz() {
        Map<String, String> map = new HashMap<>(16);
        for (SystemCommandEnum status :SystemCommandEnum.values()) {
            map.put(status.getCommandType().trim(), "cn.yangjian.im.client.service.impl.command." + status.getClazz());
        }
        return map;
    }
}
