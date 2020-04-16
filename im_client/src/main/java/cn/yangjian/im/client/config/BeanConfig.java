package cn.yangjian.im.client.config;

import cn.yangjian.im.client.handle.MsgHandleCaller;
import cn.yangjian.im.client.service.impl.MsgCallBackListener;
import cn.yangjian.im.common.constant.Constants;
import cn.yangjian.im.common.dataconstruct.RingBufferWheel;
import cn.yangjian.im.common.protocol.IMRequestProto;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class BeanConfig {

    @Value("${im.user.id}")
    private long userId;

    @Value("${im.callback.thread.queue.size}")
    private int queueSize;

    @Value("${im.callback.thread.pool.size}")
    private int poolSize;

    //    http client
    @Bean
    public OkHttpClient okHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
        return builder.build();
    }

    //    注入时间轮
    @Bean
    public RingBufferWheel bufferWheel() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        return new RingBufferWheel(executorService);
    }

    /**
     * 创建心跳单例
     * @return
     */
    @Bean(value = "heartBeat")
    public IMRequestProto.IMReqProtocol heartBeat() {
        IMRequestProto.IMReqProtocol heart = IMRequestProto.IMReqProtocol.newBuilder()
                .setRequestId(userId)
                .setReqMsg("ping")
                .setType(Constants.CommandType.PING)
                .build();
        return heart;
    }

    /**
     * 创建回调线程池
     * @return
     */
    @Bean("callBackThreadPool")
    public ThreadPoolExecutor buildCallerThread(){
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue(queueSize);
        ThreadFactory product = new ThreadFactoryBuilder()
                .setNameFormat("msg-callback-%d")
                .setDaemon(true)
                .build();
        ThreadPoolExecutor productExecutor = new ThreadPoolExecutor(poolSize, poolSize, 1, TimeUnit.MILLISECONDS, queue,product);
        return  productExecutor ;
    }


    @Bean("scheduledTask")
    public ScheduledExecutorService buildSchedule(){
        ThreadFactory sche = new ThreadFactoryBuilder()
                .setNameFormat("scheduled-%d")
                .setDaemon(true)
                .build();
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1,sche) ;
        return scheduledExecutorService ;
    }

    /**
     * 回调 bean
     * @return
     */
    @Bean
    public MsgHandleCaller buildCaller(){
        MsgHandleCaller caller = new MsgHandleCaller(new MsgCallBackListener()) ;

        return caller ;
    }
}
