package com.ck.okhttplearn.okhttp.chain;

import android.util.Log;

import com.ck.okhttplearn.BuildConfig;
import com.ck.okhttplearn.okhttp.HttpConnection;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 连接池
 */
public class ConnectionPool {

    private static final String TAG = "wsj";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * 每个连接的检查时间，可以理解成最长闲置时间
     * 假设5s
     * 每隔5s检查连接是否可用
     * 无效则将其从线程池移除
     */
    private final long keepAlive;


    private boolean cleanrunning;

    private Deque<HttpConnection> connections = new ArrayDeque<>();

    public ConnectionPool() {
        this(1, TimeUnit.MINUTES);
    }

    public ConnectionPool(long keepAlive, TimeUnit unit) {

        this.keepAlive = unit.toMillis(keepAlive);
    }

    //清理线程
    private Runnable cleanupRunable = new Runnable() {
        @Override
        public void run() {

            while (true) {
                long waitDuration = cleanup(System.currentTimeMillis());
                if (waitDuration == -1) {
                    return;
                }
                if (waitDuration > 0) {
                    synchronized (ConnectionPool.this) {
                        try {
                            ConnectionPool.this.wait(waitDuration);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };

    private long cleanup(long currentTimeMillis) {
        long longgetIdleDuration = -1;
        synchronized (this) {

            Iterator<HttpConnection> iterator = connections.iterator();
            while (iterator.hasNext()) {
                HttpConnection connection = iterator.next();
                long idleDuration = currentTimeMillis - connection.getLastUseTime();
                //超过了最大允许闲置时间
                if (idleDuration > keepAlive) {
                    if (DEBUG) Log.d(TAG, "ConnectionPool cleanup: " + "超出闲置时间，移除连接池");
                    iterator.remove();
                    connection.close();
                    continue;
                }
                //没有超过闲置时间
                //记录 最长的闲置时间
                if (longgetIdleDuration < idleDuration) {
                    longgetIdleDuration = idleDuration;
                }
            }
            //假如keepAlive是10s
            //
            if (longgetIdleDuration >= 0) {
                return keepAlive - longgetIdleDuration;
            }
            //连接池中没有连接
            cleanrunning = false;
            return longgetIdleDuration;
        }
    }

    private static final Executor executer = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "Connection Pool");
            thread.setDaemon(true);//设置为守护线程,可以理解为跟进程同样的生命周期
            return thread;
        }
    });

    /**
     * 加入连接到连接池
     * @param httpConnection
     */
    public void put(HttpConnection httpConnection) {
        //如果没有执行清理线程
        if (!cleanrunning) {
            cleanrunning = true;
            executer.execute(cleanupRunable);
        }
        connections.add(httpConnection);
    }

    /**
     * 获得满足条件可复用的连接池
     * @param host
     * @param port
     * @return
     */
    public synchronized HttpConnection get(String host, int port) {
        Iterator<HttpConnection> iterator = connections.iterator();

        while (iterator.hasNext()) {
            HttpConnection connection = iterator.next();
            //如果查找到连接池始终在相同的host和port
            if (connection.isSameAddress(host, port)) {
                iterator.remove();
                return connection;
            }
        }
        return null;
    }


}
