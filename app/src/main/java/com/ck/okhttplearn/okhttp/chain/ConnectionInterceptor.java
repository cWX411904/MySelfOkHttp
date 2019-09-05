package com.ck.okhttplearn.okhttp.chain;

import android.util.Log;

import com.ck.okhttplearn.BuildConfig;
import com.ck.okhttplearn.okhttp.HttpClient;
import com.ck.okhttplearn.okhttp.HttpConnection;
import com.ck.okhttplearn.okhttp.HttpUrl;
import com.ck.okhttplearn.okhttp.Request;
import com.ck.okhttplearn.okhttp.Response;

import java.io.IOException;

/**
 * 获得有效连接(Socket)的拦截器
 */
public class ConnectionInterceptor implements Interceptor {

    private static final String TAG = "wsj";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Override
    public Response intercept(InterceptorChain chain) throws IOException {
        if (DEBUG) Log.d(TAG, "ConnectionInterceptor intercept: " + "获取有效连接的拦截器");

        Request request = chain.call.getRequest();
        HttpClient client = chain.call.getClient();
        HttpUrl url = request.url();
        //从连接池中获得连接
        HttpConnection connection = client.getPool().get(url.getHost(), url.getPort());
        if (connection == null) {
            connection = new HttpConnection();
        } else {
            if (DEBUG) Log.d(TAG, "ConnectionInterceptor intercept: " + "从连接池中获得连接");
        }
        connection.setRequest(request);
        //执行下一个拦截器
        try {
            Response response = chain.process(connection);
            if (response.isKeepAlive()) {
                client.getPool().put(connection);
            } else {
                connection.close();
            }
            return response;
        } catch (IOException e) {
            connection.close();
            throw e;
        }

    }
}
