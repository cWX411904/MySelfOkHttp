package com.ck.okhttplearn.okhttp.chain;

import android.util.Log;

import com.ck.okhttplearn.BuildConfig;
import com.ck.okhttplearn.okhttp.HttpCode;
import com.ck.okhttplearn.okhttp.HttpConnection;
import com.ck.okhttplearn.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 要与服务器进行通信
 */
public class CallServiceInterceptor implements Interceptor {

    private static final String TAG = "wsj";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    @Override
    public Response intercept(InterceptorChain chain) throws IOException {
        if (DEBUG) Log.d(TAG, "CallServiceInterceptor intercept: " + "通信拦截器");
        HttpConnection connection = chain.httpConnection;
        HttpCode httpCode = new HttpCode();
        InputStream inputStream = connection.call(httpCode);
        String statusLine = httpCode.readLine(inputStream);
        Map<String, String> headers = httpCode.readHeaders(inputStream);
        int contentLength = -1;
        if (headers.containsKey("Content-Length")) {
            //如果有content-length，代表可以拿到响应体的字节长度
            contentLength = Integer.valueOf(headers.get("Content-Length"));
        }
        boolean isChunked = false;
        if (headers.containsKey("Transfer-Encoding")) {
            //如果有有Transfer-Encoding，表示是分块编码，此时没有响应体的长度
            isChunked = headers.get("Transfer-Encoding").equalsIgnoreCase("chunked");
        }

        String body = null;
        if (contentLength > 0) {
            byte[] bytes = httpCode.readBytes(inputStream, contentLength);
            body = new String(bytes);
        } else if (isChunked) {
            body = httpCode.readChunked(inputStream);
        }

        String[] status = statusLine.split(" ");

        boolean isKeepAlive = false;

        if (headers.containsKey("Connection")) {
            isKeepAlive = headers.get("Connection").equalsIgnoreCase("Keep-Alive");
        }
        connection.updateLastUseTime();
        return new Response(Integer.valueOf(status[1]), contentLength, headers, body,isKeepAlive);
    }
}
