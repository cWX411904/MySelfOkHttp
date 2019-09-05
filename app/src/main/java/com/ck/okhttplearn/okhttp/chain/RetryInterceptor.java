package com.ck.okhttplearn.okhttp.chain;

import android.util.Log;

import com.ck.okhttplearn.BuildConfig;
import com.ck.okhttplearn.okhttp.HttpClient;
import com.ck.okhttplearn.okhttp.Response;

import java.io.IOException;

public class RetryInterceptor implements Interceptor {

    private static final String TAG = "wsj";
    private static final boolean DEBUG = BuildConfig.DEBUG;


    @Override
    public Response intercept(InterceptorChain chain) throws IOException {
        if (DEBUG) Log.d(TAG, "RetryInterceptor intercept: " + "重试拦截器");

        IOException exception = null;

        HttpClient client = chain.call.getClient();
        for (int i = 0; i < client.getRetry() + 1; i++) {
            if (chain.call.isCancel()) {
                throw new IOException("Canceled");
            }
            Response response = null;
            try {
                //执行链条中下一个拦截器
                response = chain.process();
                return response;
            } catch (IOException e) {
                exception = e;
            }

        }

        throw exception;
    }
}
