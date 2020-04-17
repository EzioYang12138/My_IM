package cn.yangjian.im.server.util;

//		Spring容器会检测容器中的所有Bean，如果发现某个Bean实现了ApplicationContextAware接口，
//		Spring容器会在创建该Bean之后，自动调用该Bean的setApplicationContextAware()方法，
//		调用该方法时，会将容器本身作为参数传给该方法——该方法中的实现部分将Spring传入的参数（容器本身）
//		赋给该类对象的applicationContext实例变量，因此接下来可以通过该applicationContext实例变量来访问容器本身。


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public final class SpringBeanFactory implements ApplicationContextAware {

    private static ApplicationContext context;

    public static <T> T getBean(Class<T> c){
        return context.getBean(c);
    }


    public static <T> T getBean(String name,Class<T> clazz){
        return context.getBean(name,clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }


}
