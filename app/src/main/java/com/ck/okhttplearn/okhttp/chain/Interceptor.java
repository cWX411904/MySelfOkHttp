package com.ck.okhttplearn.okhttp.chain;

import com.ck.okhttplearn.okhttp.Response;

import java.io.IOException;

public interface Interceptor {

    Response intercept(InterceptorChain chain) throws IOException;
}
