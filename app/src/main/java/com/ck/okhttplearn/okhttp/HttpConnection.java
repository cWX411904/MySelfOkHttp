package com.ck.okhttplearn.okhttp;

import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

/**
 * 表示socket连接
 */
public class HttpConnection {

    Socket socket;

    Request request;

    InputStream inputStream;

    OutputStream outputStream;

    //最后使用的时间
    long lastUseTime;

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public long getLastUseTime() {
        return lastUseTime;
    }

    public void setLastUseTime(long lastUseTime) {
        this.lastUseTime = lastUseTime;
    }

    /**
     * 当前连接的socket是否与对应的host、port一致
     * @return
     */
    public boolean isSameAddress(String host, int port) {

        if (null == socket) return false;

        return TextUtils.equals(socket.getInetAddress().getHostName(), host)
                && (port == socket.getPort());
    }

    /**
     * 与服务器通信
     * @return
     */
    public InputStream call(HttpCode httpCode) throws IOException {
        //连接建立
        createSocket();
        //发送请求
        httpCode.writeRequest(outputStream, request);
        //返回服务器响应 InputStream
        return inputStream;
    }

    /**
     * 创建socket连接
     */
    private void createSocket() throws IOException{
        if (null == socket || socket.isClosed()) {
            HttpUrl url = request.url();
            if (url.protocol.equalsIgnoreCase("https")) {
                socket = SSLSocketFactory.getDefault().createSocket();
            } else {
                socket = new Socket();
            }
            socket.connect(new InetSocketAddress(url.host, url.port));
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        }
    }

    public void close() {
        if (null == socket) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void updateLastUseTime() {
        lastUseTime = System.currentTimeMillis();
    }
}
