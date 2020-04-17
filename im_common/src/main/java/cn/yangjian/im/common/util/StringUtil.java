package cn.yangjian.im.common.util;

public class StringUtil {

    public StringUtil() {
    }

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

    public static boolean isNotEmpty(String str) {
        return str != null && !"".equals(str.trim());
    }

}
