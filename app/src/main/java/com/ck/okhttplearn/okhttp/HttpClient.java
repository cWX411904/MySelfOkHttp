package com.ck.okhttplearn.okhttp;

import com.ck.okhttplearn.okhttp.chain.ConnectionPool;

public class HttpClient {

    private final Dispatcher dispatcher;
    private final ConnectionPool pool;
    private final int retry;

    public int getRetry() {
        return retry;
    }

    public ConnectionPool getPool() {
        return pool;
    }

    public Dispatcher dispatcher() {
        return dispatcher;
    }

    public HttpClient(Builder builder) {
        this.dispatcher = builder.dispatcher;
        this.retry = builder.retry;
        this.pool = builder.pool;
    }

    public Call newCall(Request request) {
        return new Call(request, this);
    }

    public static final class Builder {

        Dispatcher dispatcher;
        int retry;
        ConnectionPool pool;

        public Builder dispatcher(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
            return this;
        }

        public Builder retry(int retry) {
            this.retry = retry;
            return this;
        }

        public Builder connectionPool(ConnectionPool pool) {
            this.pool = pool;
            return this;
        }

        public HttpClient build() {
            if (null == dispatcher) {
                dispatcher = new Dispatcher();
            }
            if (null == pool) {
                pool = new ConnectionPool();
            }
            return new HttpClient(this);
        }
    }
}
