package com.ck.socket_test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MyClass {

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("restapi.amap.com", 80);

        //接收数据的输入流
        final BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    String line = null;
                    try {
                        while ((line = inputStream.readLine()) != null) {
                            System.out.println("读取：" + line);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        bufferedWriter.write("GET /v3/weather/weatherInfo?city=长沙&key=13cb58f5884f9749287abbead9c658f2 HTTP/1.1");
        bufferedWriter.write("Host: restapi.amap.com");
        bufferedWriter.flush();
    }
}
