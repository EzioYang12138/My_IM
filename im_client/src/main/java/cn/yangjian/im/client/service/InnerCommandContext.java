package cn.yangjian.im.client.service;

import cn.yangjian.im.client.service.impl.command.PrintAllCommand;
import cn.yangjian.im.client.util.SpringBeanFactory;
import cn.yangjian.im.common.enums.SystemCommandEnum;
import cn.yangjian.im.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InnerCommandContext {

    private final static Logger LOGGER = LoggerFactory.getLogger(InnerCommandContext.class);

    //    获取执行器实例
    public InnerCommand getInstance(String command) {

        Map<String, String> allClazz = SystemCommandEnum.getAllClazz();

        //兼容需要命令后接参数的数据 :q cross
        String[] trim = command.trim().split(" ");
        String clazz = allClazz.get(trim[0]);
        InnerCommand innerCommand = null;
        try {
            if (StringUtil.isEmpty(clazz)) {
                clazz = PrintAllCommand.class.getName();
            }
            innerCommand = (InnerCommand) SpringBeanFactory.getBean(Class.forName(clazz));
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
        return innerCommand;
    }
}
