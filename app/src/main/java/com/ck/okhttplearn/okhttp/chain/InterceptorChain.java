package com.ck.okhttplearn.okhttp.chain;

import com.ck.okhttplearn.okhttp.Call;
import com.ck.okhttplearn.okhttp.HttpConnection;
import com.ck.okhttplearn.okhttp.Response;

import java.io.IOException;
import java.util.List;

public class InterceptorChain {

    List<Interceptor> interceptors;
    int index;
    Call call;
    HttpConnection httpConnection;


    public InterceptorChain(List<Interceptor> interceptors, int index, Call call, HttpConnection connection) {
        this.interceptors = interceptors;
        this.index = index;
        this.call = call;
        this.httpConnection = connection;
    }

    public Response process(HttpConnection httpConnection) throws IOException{

        this.httpConnection = httpConnection;
        return process();
    }

    public Response process() throws IOException {
        if (index >= interceptors.size()) throw new IOException("Interceptor China index out max length");
        //获得拦截器 去执行
        Interceptor interceptor = interceptors.get(index);
        InterceptorChain next = new InterceptorChain(interceptors, index + 1, call, httpConnection);
        Response response = interceptor.intercept(next);

        return response;
    }
}
