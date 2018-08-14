package com.zlm.hp.async;

import android.content.Context;
import android.os.Handler;

import java.lang.ref.WeakReference;

/**
 * Created by zhangliangming on 2018-08-12.
 */

public class AsyncHandlerTask {
    private static WeakReference<Context> mContextWR;

    public AsyncHandlerTask(Context context) {
        mContextWR = new WeakReference<Context>(context);
    }

    /**
     * 执行任务
     *
     * @param uiHandler
     * @param workerHandler
     * @param workerRunnable
     * @param callback
     */
    public void execute(Handler uiHandler, Handler workerHandler, WorkerRunnable workerRunnable, final Callback callback) {
        if (isContextAlive() && workerHandler != null && workerRunnable != null) {
            if (callback != null && uiHandler != null) {
                workerRunnable.setUiHandler(uiHandler);
                workerRunnable.setCallback(callback);
            }
            workerHandler.post(workerRunnable);
        }
    }

    /**
     * 执行任务
     *
     * @param workerHandler
     * @param workerRunnable
     */
    public void execute(Handler workerHandler, WorkerRunnable workerRunnable) {
        execute(null, workerHandler, workerRunnable, null);
    }

    /**
     * WorkerRunnable
     */
    public static class WorkerRunnable implements Runnable {
        private Handler uiHandler;
        private Callback callback;

        @Override
        public void run() {

        }

        /**
         *
         */
        public void runCallBackTask() {
            if (isContextAlive() && callback != null && uiHandler != null) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.runOnUiThread();
                    }
                });
            }
        }

        public void setUiHandler(Handler uiHandler) {
            this.uiHandler = uiHandler;
        }

        public void setCallback(Callback callback) {
            this.callback = callback;
        }
    }

    /**
     * @return
     */
    private static boolean isContextAlive() {
        Context context = mContextWR.get();
        return context != null;
    }

    /**
     *
     */
    public interface Callback {
        void runOnUiThread();
    }
}
