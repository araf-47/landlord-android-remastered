package com.landlord.android.core.common;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppExecutors {

    public static final ExecutorService DB = Executors.newFixedThreadPool(4);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private AppExecutors() {}

    public static void runOnMain(Runnable runnable) {
        MAIN_HANDLER.post(runnable);
    }
}
