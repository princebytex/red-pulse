package com.redpulse.app.utils;

public interface FirestoreCallback<T> {
    void onSuccess(T data);
    void onError(String message);
}
