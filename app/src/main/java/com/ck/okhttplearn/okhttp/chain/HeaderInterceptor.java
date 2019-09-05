package com.ck.okhttplearn.okhttp.chain;

import android.util.Log;

import com.ck.okhttplearn.BuildConfig;
import com.ck.okhttplearn.okhttp.Call;
import com.ck.okhttplearn.okhttp.Request;
import com.ck.okhttplearn.okhttp.RequestBody;
import com.ck.okhttplearn.okhttp.Response;

import java.io.IOException;
import java.util.Map;

public class HeaderInterceptor implements Interceptor {

    private static final String TAG = "wsj";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Override
    public Response intercept(InterceptorChain chain) throws IOException {

        if (DEBUG) Log.d(TAG, "HeaderInterceptor intercept: " + "配置请求头的拦截器");

        Call call = chain.call;
        Request request = call.getRequest();
        //如果用户没有配置请求头
        Map<String, String> headers = request.headers();
        if (!headers.containsKey("Connection")) {
            headers.put("Connection", "Keep-Alive");
        }
        headers.put("Host", request.url().getHost());
        //是否有请求体
        if (null != request.body()) {
            RequestBody body = request.body();
            long contentLength = body.contentLength();
            if (contentLength != 0) {
                headers.put("Content-length", String.valueOf(contentLength));
            }
            String contentType = body.contentType();
            if (null != contentType) {
                headers.put("Content-Type", contentType);
            }
        }
        return chain.process();
    }
}
