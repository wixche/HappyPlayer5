package com.zlm.hp.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;


/**
 * 弹窗口提示
 */
public class ToastUtil {
    private static Toast toast;

    public static void showTextToast(Context context, String msg) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setText(msg);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    public static void showCenterTextToast(Context context, String msg) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setText(msg);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void release(){
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
    }

}
