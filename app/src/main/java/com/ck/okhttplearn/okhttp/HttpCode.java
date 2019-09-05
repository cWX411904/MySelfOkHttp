package com.ck.okhttplearn.okhttp;

import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * 用来处理Socket请求头请求体的拼接
 */
public class HttpCode {

    /**
     * 请求行
     * GET /v3/weather/weatherInfo?city=%E9%95%BF%E6%B2%99&key=13cb58f5884f9749287abbead9c658f2 HTTP/1.1\r\n
     *
     * 请求头
     * Host: restapi.amap.com\r\n
     * \r\n
     *
     * 响应头
     * HTTP/1.1 200 OK\r\n
     * date: Fri, 27 Apr 2018 13:48:33 GMT\r\n
     * sc: 0.010
     * server: Tengine
     * x-powered-by: ring/1.0.0
     * access-control-allow-methods: *
     * content-type: application/json;charset=UTF-8
     * access-control-allow-origin: *
     * connection: close
     * gsid: 010177155230152483691307700322917275607
     * access-control-allow-headers: DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,key,x-biz,x-info,platinfo,encr,enginever,gzipped,poiid
     * content-length: 445
     * \r\n
     *
     * 响应体
     * {"status":"1","count":"2","info":"OK","infocode":"10000","lives":[{"province":"湖南","city":"长沙市","adcode":"430100","weather":"多云","temperature":"19","winddirection":"北","windpower":"≤3","humidity":"87","reporttime":"2018-04-27 21:00:00"},{"province":"湖南","city":"长沙县","adcode":"430121","weather":"多云","temperature":"19","winddirection":"北","windpower":"≤3","humidity":"87","reporttime":"2018-04-27 21:00:00"}]}
     */

    //回车和换行
    static final String CRLF = "\r\n";
    static final int CR = 13;
    static final int LF = 10;
    static final String SPACE = " ";
    static final String VERSION = "HTTP/1.1";
    static final String COLON = ":";


    public static final String HEAD_HOST = "Host";
    public static final String HEAD_CONNECTION = "Connection";
    public static final String HEAD_CONTENT_TYPE = "Content-Type";
    public static final String HEAD_CONTENT_LENGTH = "Content-Length";
    public static final String HEAD_TRANSFER_ENCODING = "Transfer-Encoding";

    public static final String HEAD_VALUE_KEEP_ALIVE = "Keep-Alive";
    public static final String HEAD_VALUE_CHUNKED = "chunked";

    ByteBuffer byteBuffer;

    public HttpCode() {
        byteBuffer = ByteBuffer.allocate(10 * 1024);
    }

    public void writeRequest(OutputStream outputStream, Request request) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        //拼请求行
        stringBuffer.append(request.method());
        stringBuffer.append(SPACE);
        stringBuffer.append(request.url().file);
        stringBuffer.append(SPACE);
        stringBuffer.append(VERSION);
        stringBuffer.append(CRLF);

        //拼请求头
        Map<String, String> headers = request.headers();

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            stringBuffer.append(entry.getKey());
            stringBuffer.append(COLON);
            stringBuffer.append(SPACE);
            stringBuffer.append(entry.getValue());
            stringBuffer.append(CRLF);
        }
        stringBuffer.append(CRLF);

        //拼请求体
        RequestBody body = request.body();
        if (null != body) {
            stringBuffer.append(body.body());
        }
        outputStream.write(stringBuffer.toString().getBytes());
        outputStream.flush();
    }

    public String readLine(InputStream is) throws IOException {
        byteBuffer.clear();

        //标记
        byteBuffer.mark();

        boolean isMabeyEofLine = false;
        byte b;
        while ((b = (byte) is.read()) != -1) {
            byteBuffer.put(b);
            //如果读到当前是/r
            if (b == CR) {
                isMabeyEofLine = true;
            } else if (isMabeyEofLine) {
                //读到/n
                if (b == LF) {
                    //当前行结束,保存一行数据
                    byte[] lineBytes = new byte[byteBuffer.position()];
                    byteBuffer.reset();
                    //从byteBuffer获得数据
                    byteBuffer.get(lineBytes);
                    byteBuffer.clear();
                    byteBuffer.mark();
                    return new String(lineBytes);
                }
                isMabeyEofLine = false;
            }
        }
        throw new IOException("Response read line");
    }

    public Map<String, String> readHeaders(InputStream is) throws IOException {

        HashMap<String, String> headers = new HashMap<>();

        while (true) {
            String line = readLine(is);
            if (isEmptyLine(line)) {
                //如果当前是空的一行，代表响应头结束
                break;
            }
            int index = line.indexOf(":");
            if (index > 0) {
                //响应头的key
                String key = line.substring(0, index);
                //+2是因为key与value之间有个冒号和空格，-2是因为每行都是以/r/n结束的
                String value = line.substring(index + 2, line.length() - 2);
                headers.put(key, value);
            }
        }
        return headers;
    }

    private boolean isEmptyLine(String line) {
        return TextUtils.equals(line, CRLF);
    }

    public byte[] readBytes(InputStream is, int len) throws IOException {
        byte[] bytes = new byte[len];
        int readNum = 0;
        while(true) {
            readNum += is.read(bytes, readNum, len - readNum);
            if (readNum == len) {
                return bytes;
            }
        }
    }

    public String readChunked(InputStream inputStream) throws IOException {
        int len = -1;
        boolean isEmpthData = false;
        StringBuffer chunked = new StringBuffer();
        while (true) {
            if (len < 0) {

                String line = readLine(inputStream);
                //
                line = line.substring(0, line.length() - 2);
                //获得长度16进制的字符串，转成10进制的整型
                len = Integer.valueOf(line, 16);
                isEmpthData = len == 0;
            } else {
                byte[] bytes = readBytes(inputStream, len + 2);
                chunked.append(new String(bytes));
                len = -1;
                if (isEmpthData) {
                    return chunked.toString();
                }
            }
        }
    }
}











