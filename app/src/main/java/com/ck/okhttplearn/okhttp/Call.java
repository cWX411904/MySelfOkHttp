package com.ck.okhttplearn.okhttp;

import com.ck.okhttplearn.okhttp.chain.CallServiceInterceptor;
import com.ck.okhttplearn.okhttp.chain.ConnectionInterceptor;
import com.ck.okhttplearn.okhttp.chain.HeaderInterceptor;
import com.ck.okhttplearn.okhttp.chain.Interceptor;
import com.ck.okhttplearn.okhttp.chain.InterceptorChain;
import com.ck.okhttplearn.okhttp.chain.RetryInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Call {

    Request request;

    HttpClient client;

    //是否执行过
    boolean executed;

    boolean cancel;

    public boolean isCancel() {
        return cancel;
    }

    public Request getRequest() {
        return request;
    }

    public Call(Request request, HttpClient client) {
        this.request = request;
        this.client = client;
    }

    public HttpClient getClient() {
        return client;
    }

    public void enqueue(Callback callback) {
        synchronized (this) {
            if (executed) {
                throw new IllegalStateException("已经执行过了，就不要执行");
            }
            executed = true;
        }
        //把任务交给调度器调度
        client.dispatcher().enqueue(new AsyncCall(callback));
    }

    /**
     * 是否取消
     */
    public void cancel() {
        cancel = true;
    }

    /**
     * 执行网络请求的线程
     */
    class AsyncCall implements Runnable {

        private Callback callback;

        public AsyncCall(Callback callback) {
            this.callback = callback;
        }

        @Override
        public void run() {


            //是否回调过
            boolean singaledCallbacked = false;
            try {
                //执行真正的请求
                Response response = getResponse();
                if (cancel) {
                    singaledCallbacked = true;
                    callback.onFailure(Call.this, new IOException("客户端主动执行了cancel"));
                } else {
                    singaledCallbacked = true;
                    callback.onResponse(Call.this, response);
                }
            } catch (Exception e) {
//                e.printStackTrace();
                if (!singaledCallbacked) {
                    //如果没有回调过
                    callback.onFailure(Call.this, e);
                }
            } finally {
                //将这个任务从调度器移除
                client.dispatcher().finished(this);
            }
        }



        public String host() {
            return request.url().getHost();
        }
    }

    /**
     * 这里是重点！！！
     * @return
     */
    private Response getResponse() throws Exception{
        //创建拦截器责任链
        List<Interceptor> interceptors = new ArrayList();
        //重试拦截器
        interceptors.add(new RetryInterceptor());
        //请求头拦截器
        interceptors.add(new HeaderInterceptor());
        //连接拦截器
        interceptors.add(new ConnectionInterceptor());
        //通信拦截器
        interceptors.add(new CallServiceInterceptor());
        InterceptorChain chain = new InterceptorChain(interceptors, 0, this, null);
        return chain.process();
    }
}
