package zy.example.com.expandingtextview;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * /*@Description
 * /*created by wwq on 2018/11/29 0029
 * /*@company zhongyiqiankun
 */
public class ExpandingTextView extends android.support.v7.widget.AppCompatTextView {

    private static final String TAG = "ExpandingTextView";
//    控件宽度
    private int width;
//    控件折叠的高度
    private int minHeight;
//    控件展开的高度
    private int maxHeight;
//    原始文本
    private String origin;
//    展开的按钮span
    private SpannableString openExpand;
//    折叠的按钮span
    private SpannableString closeExpand;
//    展开span的文本说明
    private String openText="...全文";
//    折叠span的文本说明
    private String closeText="...收起";
//    展开span的文本颜色
    private int openTextColor;
//    折叠span的文本颜色
    private int closeTextColor;
//    折叠span前填充的空格字符串
    private StringBuilder space;
//    折叠文本时，原始文本截取后的字符串
    private String substring;
//    只有在原始文本onDraw后，才会仅且添加1次折叠处理
    private boolean drawEnd;
//    折叠文本时显示的最大行数
    private int maxLines = 1;

    public ExpandingTextView(Context context, @org.jetbrains.annotations.Nullable @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta=context.obtainStyledAttributes(attrs,R.styleable.ExpandingTextView);
        String result=ta.getString(R.styleable.ExpandingTextView_open_Text);
        if (result!=null){
            openText=result;
        }
        result=ta.getString(R.styleable.ExpandingTextView_close_Text);
        if (result!=null){
            closeText=result;
        }
        int color=ta.getResourceId(R.styleable.ExpandingTextView_openText_Color,R.color.colorPrimary);
        openTextColor=color;
        color=ta.getResourceId(R.styleable.ExpandingTextView_closeText_Color,R.color.colorPrimary);
        closeTextColor=color;
        ta.recycle();
        initText();
    }

    @Override
    public void setMaxLines(int maxLines) {
//        取消父方法实现
        this.maxLines=maxLines;
    }


    @Override
    public void setHeight(int height) {
//        为属性动画增加设置器
        this.getLayoutParams().height=height;
        requestLayout();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (width == 0) {
            width = getMeasuredWidth();
            maxHeight = getMeasuredHeight();
        }
        if (minHeight == 0) {
            minHeight = getMeasuredHeight();
        }
        minHeight = Math.min(minHeight, getMeasuredHeight());
        maxHeight = Math.max(maxHeight, getMeasuredHeight());
        if (!drawEnd){
         drawEnd=true;
         closeExpandText();
        }
    }


    //——————————————————————————————————————————
//    初始化参数变量
    private void initText() {
        origin = getText().toString();
        Log.i(TAG, "initText: result=" + origin);
        openExpand = new SpannableString(openText);
        ButtonSpan openButtonSpan = new ButtonSpan(getContext(), new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: ");
                openExpandText();
            }
        }, openTextColor);
        openExpand.setSpan(openButtonSpan, 0, openExpand.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        closeExpand = new SpannableString(closeText);
        ButtonSpan closeButtonSpan = new ButtonSpan(getContext(), new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeExpandText();
//                设置动画效果
                ValueAnimator valueAnimator=ValueAnimator.ofInt( maxHeight,minHeight)
                        .setDuration(300);
                valueAnimator.setTarget(this);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int height=(int)animation.getAnimatedValue();
                        setHeight(height);
                    }
                });
                valueAnimator.start();
            }
        }, closeTextColor);
        closeExpand.setSpan(closeButtonSpan, 0, closeExpand.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        append("\n");
    }



//    ____________________________________
    //      折叠全文
    private void closeExpandText() {
        Layout originLayout = createWorkingLayout(origin, width);
        if (substring == null) {
            if (originLayout.getLineCount() > maxLines) {
                int length = openExpand.length();
                int expandTextLength = originLayout.getLineEnd(maxLines) - length;
                if (expandTextLength > 0) {
                    substring = origin.substring(0, expandTextLength - length);
                } else {
                    Log.e(TAG, "onDraw: the length of openExpand over the origin text");
                    substring = origin;
                }
            }
        }

        setText(substring);
//            让span字符串生效
        append(openExpand);
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    //    展开全文
    private void openExpandText() {

        if (space == null) {
            space = new StringBuilder(" ");
            Layout layout = createWorkingLayout(space.toString() + closeExpand, width);
//        偏移span按钮到下一行的最后
            while (layout.getLineCount() < 2) {
                space.append(" ");
                layout = createWorkingLayout(space.toString() + closeExpand, width);
            }
            space.delete(space.length() - 2, space.length() - 1);
        }
//        展开原文
        setText(origin);
        append("\n");
        append(space);
        append(closeExpand);
        setMovementMethod(LinkMovementMethod.getInstance());
        ValueAnimator valueAnimator=ValueAnimator.ofInt(minHeight, maxHeight)
                .setDuration(500);
        valueAnimator.setTarget(this);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height=(int)animation.getAnimatedValue();
                setHeight(height);
            }
        });
        valueAnimator.start();
    }


    //返回textView的显示区域的layout，该textView的layout并不会显示出来，只是用其宽度来比较要显示的文字是否过长
    private Layout createWorkingLayout(String workingText, int initWidth) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new StaticLayout(workingText, getPaint(), initWidth - getPaddingLeft() - getPaddingRight(),
                    Layout.Alignment.ALIGN_NORMAL, getLineSpacingMultiplier(), getLineSpacingExtra(), false);
        } else {
            StaticLayout.Builder builder = StaticLayout.Builder.obtain(workingText, 0, workingText.length(), getPaint(), initWidth - getPaddingLeft() - getPaddingRight());
            builder.build();
            return builder.build();
        }
    }
}
