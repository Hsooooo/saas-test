package com.illunex.emsaasrestapi.common.aws;

public interface AwsSESListener<T> {
    public abstract void onSuccess(T t);
    public abstract void onFail(T t);
}
