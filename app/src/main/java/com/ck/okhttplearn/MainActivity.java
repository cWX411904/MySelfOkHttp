package com.ck.okhttplearn;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ck.okhttplearn.okhttp.Call;
import com.ck.okhttplearn.okhttp.Callback;
import com.ck.okhttplearn.okhttp.HttpClient;
import com.ck.okhttplearn.okhttp.Request;
import com.ck.okhttplearn.okhttp.RequestBody;
import com.ck.okhttplearn.okhttp.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "wsj";
    private static final boolean DEBUG = BuildConfig.DEBUG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void get(View view) {
        //第一步
        HttpClient client = new HttpClient.Builder().retry(2).build();
        //第二步
        Request request = new Request.Builder()
                .url("http://www.kuaidi100.com/query?type=yuantong&postid=222222222")
                .build();

        //第三步
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, Throwable throwable) {
                if (DEBUG) Log.d(TAG, "MainActivity onFailure: " + "" + throwable);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (DEBUG) Log.d(TAG, "MainActivity onResponse: " + "" + response.getBody());
            }
        });


    }

    public void post(View view) {
        HttpClient client = new HttpClient.Builder().retry(2).build();
        RequestBody body = new RequestBody()
                .add("city", "长沙")
                .add("key", "13cb58f5884f9749287abbead9c658f2");

        Request request = new Request.Builder().url("http://restapi.amap" +
                ".com/v3/weather/weatherInfo").post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                Log.e("响应体", response.getBody());
            }
        });
    }
}
