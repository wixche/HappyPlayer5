package com.zlm.hp.async;

import com.zlm.hp.handler.WeakRefHandler;


/**
 * 异步handler任务
 * Created by zhangliangming on 2018-08-12.
 */

public class AsyncHandlerTask {

    private WeakRefHandler mUIHandler;
    private WeakRefHandler mWorkerHandler;

    public AsyncHandlerTask(WeakRefHandler uiHandler, WeakRefHandler workerHandler) {
        this.mUIHandler = uiHandler;
        this.mWorkerHandler = workerHandler;
    }

    /**
     * 执行后台任务
     *
     * @param task 任务
     */
    public void execute(Task task) {
        execute(mUIHandler, mWorkerHandler, task);
    }


    /**
     * 执行后台任务，子线程返回或者主线程返回
     *
     * @param uiHandler
     * @param workerHandler
     * @param task
     */
    private void execute(final WeakRefHandler uiHandler, final WeakRefHandler workerHandler, final Task task) {
        if (workerHandler != null) {
            workerHandler.post(new Runnable() {
                @Override
                public void run() {
                    Object result = null;
                    if (workerHandler.isAlive()) {
                        if (task != null) {
                            result = task.doInBackground();
                        }
                    }
                    if (uiHandler != null) {
                        final Object finalResult = result;
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (uiHandler.isAlive()) {
                                    if (task != null) {
                                        task.onPostExecute(finalResult);
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * 工作任务
     */
    public static abstract class Task<T> {
        /**
         * 后台执行
         *
         * @return
         */
        protected abstract T doInBackground();

        /**
         * 主线程回调
         *
         * @param result
         */
        protected void onPostExecute(T result) {
        }
    }
}
