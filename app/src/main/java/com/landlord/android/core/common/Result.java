package com.landlord.android.core.common;

public abstract class Result<T> {

    public static class Loading<T> extends Result<T> {}

    public static class Success<T> extends Result<T> {
        public final T data;

        public Success(T data) {
            this.data = data;
        }
    }

    public static class Error<T> extends Result<T> {
        public final String message;
        public final Throwable error;

        public Error(String message) {
            this(message, null);
        }

        public Error(String message, Throwable error) {
            this.message = message;
            this.error = error;
        }
    }

    public static <T> Result<T> loading() {
        return new Loading<>();
    }

    public static <T> Result<T> success(T data) {
        return new Success<>(data);
    }

    public static <T> Result<T> error(String message) {
        return new Error<>(message);
    }
}
