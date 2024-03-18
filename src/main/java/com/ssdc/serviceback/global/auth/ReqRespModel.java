package com.ssdc.serviceback.global.auth;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ReqRespModel<T> implements IReqRespModel<T> {
    private final T data;
    private final String message;
    private final long timestamp;

    public ReqRespModel(T data, String message) {
        this.data = data;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public T getData() {
        return this.data;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
