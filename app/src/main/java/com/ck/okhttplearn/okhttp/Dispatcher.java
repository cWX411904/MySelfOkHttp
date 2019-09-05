package com.ck.okhttplearn.okhttp;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Dispatcher {

    //同时进行最大请求数 默认64
    private int maxRequest = 64;

    //同时进行最大相同的host请求数
    private int maxRequestsPreHost = 5;

    //等待执行队列
    private Deque<Call.AsyncCall> readyAsyncCalls = new ArrayDeque<>();

    //正在执行队列
    private Deque<Call.AsyncCall> runningAsyncCalls = new ArrayDeque<>();

    //线程池
    private ExecutorService executorService;

    public synchronized ExecutorService executorService() {

        if (executorService == null) {

            ThreadFactory threadFactory = new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "Http Client");
                }
            };

            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                    60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
                    threadFactory);
        }

        return executorService;
    }

    public Dispatcher() {
    }

    public Dispatcher(int maxRequest, int maxRequestsPreHost) {
        this.maxRequest = maxRequest;
        this.maxRequestsPreHost = maxRequestsPreHost;
    }

    public void enqueue(Call.AsyncCall call) {

        if (runningAsyncCalls.size() < maxRequest && runningCallsForHost(call) < maxRequestsPreHost) {
            //满足条件马上开始执行任务
            runningAsyncCalls.add(call);
            executorService().execute(call);
        } else {
            readyAsyncCalls.add(call);
        }
    }

    private int runningCallsForHost(Call.AsyncCall call) {
        int result = 0;
        for (Call.AsyncCall runningAsyncCall : runningAsyncCalls) {
            if (runningAsyncCall.host().equals(call.host())) {
                result++;
            }
        }
        return result;
    }

    /**
     * 表示一个请求成功
     * 将其从runningAsync移除
     * 并且检查ready是否可以执行
     * @param call
     */
    public void finished(Call.AsyncCall call) {

        synchronized (this) {
            runningAsyncCalls.remove(call);
            checkReady();
        }

    }

    private void checkReady() {
        if (runningAsyncCalls.size() >= maxRequest) {
            return;
        }
        if (readyAsyncCalls.isEmpty()) return;

        Iterator<Call.AsyncCall> iterator = readyAsyncCalls.iterator();
        while (iterator.hasNext()) {
            //获得一个等待执行的任务
            Call.AsyncCall asyncCall = iterator.next();
            //如果获得的等待执行的任务，执行后小于host相同最大允许数
            if (runningCallsForHost(asyncCall) < maxRequestsPreHost) {
                iterator.remove();
                runningAsyncCalls.add(asyncCall);
                executorService().execute(asyncCall);
            }
            //如果正在执行的任务达到了最大
            if (runningAsyncCalls.size() >= maxRequest) {
                return;
            }
        }
    }
}
