package zy.example.com.expandingtextview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * /*@Description
 * /*created by wwq on 2018/11/29 0029
 * /*@company zhongyiqiankun
 */
public class ButtonSpan extends ClickableSpan {

    private View.OnClickListener onClickListener;
    private Context context;
    private int colorId;

    public ButtonSpan(Context context, View.OnClickListener onClickListener,  int colorId) {
        this.onClickListener = onClickListener;
        this.context = context;
        this.colorId = colorId;
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
//        给buttonSpan设置颜色，取消下划线
        ds.setColor(context.getResources().getColor(colorId));
        ds.setUnderlineText(false);
    }

    @Override
    public void onClick( @NonNull View widget) {
        if (onClickListener!=null){
            onClickListener.onClick(widget);
        }
    }
}
