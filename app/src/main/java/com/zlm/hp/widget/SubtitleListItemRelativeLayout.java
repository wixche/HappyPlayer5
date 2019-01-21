package com.zlm.hp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.zlm.hp.util.ColorUtil;

/**
 * listview item
 */
public class SubtitleListItemRelativeLayout extends RelativeLayout {

    private int defColor;
    private int pressColor;


    public SubtitleListItemRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public SubtitleListItemRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SubtitleListItemRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        defColor = ColorUtil.parserColor("#000000", 50);
        pressColor = ColorUtil.parserColor("#000000", 80);
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
