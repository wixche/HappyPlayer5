package com.zlm.hp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.zlm.hp.util.ColorUtil;

/**
 * listview item
 */
public class ListItemRelativeLayout extends RelativeLayout {

    private int defColor;
    private int pressColor;


    public ListItemRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ListItemRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ListItemRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        defColor = ColorUtil.parserColor("#ffffff", 255);
        pressColor = ColorUtil.parserColor("#e1e1e1", 255);
        setBackgroundColor(defColor);
    }

    public void setPressed(boolean pressed) {
        super.setPressed(pressed);

        if (pressed) {
            setBackgroundColor(pressColor);
        } else {
            setBackgroundColor(defColor);
        }
    }

}
