package cn.yangjian.im.common.dataconstruct;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class RingBufferWheel {

    //首先我们要定义一个 Task 类，用于抽象任务；它本身也是一个线程
//    将业务逻辑写在 run() 中即可。
    @Data
    public abstract static class Task extends Thread {
        //        指示任务下标
        private int index;

        //        用于记录该任务所在时间轮的圈数。
        private int cycleNum;

        //        在这里其实就是延时时间。
        private int key;

        @Override
        public void run() {
        }
    }

    private final Logger logger = LoggerFactory.getLogger(RingBufferWheel.class);

    //    时间轮的大小  默认 64个格子
    private static final int STATIC_RING_SIZE = 64;

    //    时间轮里边存放的对象
//    实际上每一个下标存放的是一个set集合，set集合里边包含着task
    private Object[] ringBuffer;

    private int bufferSize;

    //    线程池
    private final ExecutorService executorService;

    //    任务的实际数量
    private volatile int size = 0;

    //    任务结束的标志
    private volatile boolean stop = false;

    //    任务开始的标志
    private final AtomicBoolean start = new AtomicBoolean(false);

    /**
     * total tick times
     */
    private final AtomicInteger tick = new AtomicInteger();

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    private final AtomicInteger taskId = new AtomicInteger();
    private final Map<Integer, Task> taskMap = new HashMap<>(16);


    //    创建一个默认大小的时间轮 64 个格子
    public RingBufferWheel(ExecutorService executorService) {
        this.executorService = executorService;
        this.bufferSize = STATIC_RING_SIZE;
        this.ringBuffer = new Object[bufferSize];
    }


    //    创建一个给定数量的时间轮，要求数量是2的n次方
    public RingBufferWheel(ExecutorService executorService, int bufferSize) {
        this(executorService);
        if (!powerOf2(bufferSize)) {
            throw new RuntimeException("bufferSize=[" + bufferSize + "] must be a power of 2");
        }
        this.bufferSize = bufferSize;
        this.ringBuffer = new Object[bufferSize];
    }

    //    向时间轮中加入任务，这是线程安全的
    public void addTask(Task task) {
        int key = task.getKey(); //任务延时时间
        int id;

        try {
            lock.lock();
//            下标是key取余格子数量大小
            int index = mod(key, bufferSize);
            task.setIndex(index);
//            获取这个位置处的所有的index,index是下标，不是真实时间
            Set<Task> tasks = get(index);

//            index这个下标之前有task
            if (tasks != null) {
                int cycleNum = cycleNum(key, bufferSize);
                task.setCycleNum(cycleNum);
                tasks.add(task);
            } else {
//                index这个下标之前没有task
//                圈数就是key除以size
                int cycleNum = cycleNum(key, bufferSize);
                task.setIndex(index);
                task.setCycleNum(cycleNum);
                Set<Task> sets = new HashSet<>();
                sets.add(task);
                put(key, sets);
            }
            id = taskId.incrementAndGet();
            taskMap.put(id, task);
            size++;
        } finally {
            lock.unlock();
        }

        start();

    }

    //    开启一个线程来消费时间轮，只要不调用stop方法就一直消费下去
    public void start() {
        if (!start.get()) {

            if (start.compareAndSet(start.get(), true)) {
                logger.info("延时任务已开启");
                Thread job = new Thread(new TriggerJob());
                job.setName("消费时间轮的线程");
                job.start();
                start.set(true);
            }

        }
    }

    public int taskSize() {
        return size;
    }

    //    停止消费时间轮
    public void stop(boolean force) {
        if (force) {
            logger.info("延时任务已经停止");
            stop = true;
            executorService.shutdownNow();
        } else {
            logger.info("任务正在停止");
            if (taskSize() > 0) {
                try {
                    lock.lock();
                    condition.await();
                    stop = true;
                } catch (InterruptedException e) {
                    logger.error("InterruptedException", e);
                } finally {
                    lock.unlock();
                }
            }
            executorService.shutdown();
        }
    }

//    根据下标获取所有task
    private Set<Task> get(int index) {
        return (Set<Task>) ringBuffer[index];
    }

    private void put(int key, Set<Task> tasks) {
//        key是真实时间
        int index = mod(key, bufferSize);
        ringBuffer[index] = tasks;
    }

//    将index下标下的第一圈的任务放到result里，其余的任务减去一圈放回原处
    private Set<Task> remove(int index) {
        Set<Task> tempTask = new HashSet<>();
        Set<Task> result = new HashSet<>();

        Set<Task> tasks = (Set<Task>) ringBuffer[index];
        if (tasks == null) {
            return result;
        }

        for (Task task : tasks) {
            if (task.getCycleNum() == 0) {
                result.add(task);

                size2Notify();
            } else {
                // decrement 1 cycle number and update origin data
                task.setCycleNum(task.getCycleNum() - 1);
                tempTask.add(task);
            }
        }

        //update origin data
        ringBuffer[index] = tempTask;

        return result;
    }

    private void size2Notify() {
        try {
            lock.lock();
            size--;
            if (size == 0) {
                condition.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    private int mod(int target, int mod) {
        // equals target % mod
        target = target + tick.get();
        return target & (mod - 1);
    }

    private int cycleNum(int target, int mod) {
        //equals target/mod
        return target >> Integer.bitCount(mod - 1);
    }

    private boolean powerOf2(int target) {
        if (target < 0) {
            return false;
        }
//        2的N次方。用二进制表示都是10，100，1000……
        int value = target & (target - 1);
        if (value != 0) {
            return false;
        }

        return true;
    }

    private class TriggerJob implements Runnable {

        @Override
        public void run() {
            int index = 0;
            while (!stop) {
                try {
                    Set<Task> tasks = remove(index);
                    for (Task task : tasks) {
                        executorService.submit(task);
                    }

                    if (++index > bufferSize - 1) {
                        index = 0;
                    }

                    //Total tick number of records
                    tick.incrementAndGet();
                    TimeUnit.SECONDS.sleep(1);

                } catch (Exception e) {
                    logger.error("Exception", e);
                }

            }

            logger.info("delay task is stopped");
        }
    }
}
