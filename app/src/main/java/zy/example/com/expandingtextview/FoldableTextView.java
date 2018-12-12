package zy.example.com.expandingtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * /*@Description
 * /*created by wwq on 2018/11/29 0029
 * /*@company zhongyiqiankun
 */
public class FoldableTextView extends ViewGroup {

    private static final String TAG = "FoldableTextView";

    private TextView mTextView;
    private TextView mTextButton;
    private String originText;
    private int mWidth;
    private int maxLine;
    private int thisPadding;
    private float textSize;
    //    展开的按钮span
    private SpannableString openExpand;
    //    折叠的按钮span
    private SpannableString closeExpand;
    //    展开span的文本说明
    private String openText="...全文";
    //    折叠span的文本说明
    private String closeText="...收起";
    private String substring;
    private int openTextColor;
    private int closeTextColor;
    private boolean folded;
    private StringBuilder space;


    public FoldableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta=context.obtainStyledAttributes(attrs,R.styleable.FoldableTextView);
        String result=ta.getString(R.styleable.FoldableTextView_openText);
        if (result!=null){
            openText=result;
        }
        result=ta.getString(R.styleable.FoldableTextView_closeText);
        if (result!=null){
            closeText=result;
        }
        openTextColor=ta.getResourceId(R.styleable.FoldableTextView_openTextColor,R.color.colorPrimary);
        closeTextColor=ta.getResourceId(R.styleable.FoldableTextView_closeTextColor,R.color.colorPrimary);
        maxLine=ta.getResourceId(R.styleable.FoldableTextView_maxLine,3);
        thisPadding=ta.getResourceId(R.styleable.FoldableTextView_thisPadding,10);
        textSize =ta.getInteger(R.styleable.FoldableTextView_textSize,12);
        ta.recycle();
        initView(context);
    }

//    添加textView
    private void initView(Context context) {
        mTextView=new TextView(context);
        mTextView.setTextSize(textSize);
        Log.i(TAG, "initView: textSize="+textSize);
        LayoutParams lp=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        addView(mTextView,lp);

        mTextButton=new TextView(context);
        mTextButton.setTextSize(textSize);
        addView(mTextButton,lp);

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
//                ValueAnimator valueAnimator=ValueAnimator.ofInt( maxHeight,minHeight)
//                        .setDuration(300);
//                valueAnimator.setTarget(this);
//                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator animation) {
//                        int height=(int)animation.getAnimatedValue();
//                        setHeight(height);
//                    }
//                });
//                valueAnimator.start();
            }
        }, closeTextColor);
        closeExpand.setSpan(closeButtonSpan, 0, closeExpand.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

    }


    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

//        只支持height wrap模式
        if (mWidth==0){
            mWidth=MeasureSpec.getSize(widthMeasureSpec);
        }
        setPadding(thisPadding*2,thisPadding*2,thisPadding*2,thisPadding*2);
        int mHeight = getPaddingTop();
        int count=getChildCount();
        for (int i=0;i<count;i++){
            View view=getChildAt(i);
            if (view.getVisibility()!=GONE){
                measureChild(view,widthMeasureSpec,heightMeasureSpec);
                int height=view.getMeasuredHeight();
                mHeight +=height+getPaddingTop();
            }
        }
        setMeasuredDimension(mWidth, mHeight);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int sumLeft=getPaddingLeft();
        int sumTop=getPaddingTop();
        int count=getChildCount();
        for (int i=0;i<count;i++) {
            View view = getChildAt(i);
            if (view.getVisibility() != GONE) {
                int width=view.getMeasuredWidth();
                int height=view.getMeasuredHeight();
                view.layout(sumLeft,sumTop,sumLeft+width,sumTop+height);
                sumTop+=height+getPaddingTop();
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (folded){
            closeExpandText();
        }
    }

    //返回textView的显示区域的layout，该textView的layout并不会显示出来，只是用其宽度来比较要显示的文字是否过长
    private Layout createWorkingLayout(String workingText) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new StaticLayout(workingText, mTextView.getPaint(), mWidth - getPaddingLeft() - getPaddingRight(),
                    Layout.Alignment.ALIGN_NORMAL, mTextView.getLineSpacingMultiplier(), mTextView.getLineSpacingExtra(), false);
        } else {
            StaticLayout.Builder builder = StaticLayout.Builder.obtain(workingText, 0, workingText.length(), mTextView.getPaint(), mWidth - getPaddingLeft() - getPaddingRight());
            builder.build();
            return builder.build();
        }
    }

    private void openExpandText() {
        if (space == null) {
            space = new StringBuilder(" ");
            Layout layout = createWorkingLayout(space.toString() + closeExpand);
//        偏移span按钮到下一行的最后
            while (layout.getLineCount() < 2) {
                space.append(" ");
                layout = createWorkingLayout(space.toString() + closeExpand);
            }
            space.delete(space.length() - 2, space.length() - 1);
            mTextButton.setText(space);
            mTextButton.append(closeExpand);
            mTextButton.setMovementMethod(LinkMovementMethod.getInstance());
        }
        mTextView.setText(originText);
        mTextButton.setVisibility(VISIBLE);
    }

    private void closeExpandText() {
        Layout originLayout = createWorkingLayout(originText);
        if (substring == null) {
            if (originLayout.getLineCount() > maxLine) {
                int length = openExpand.length()+2;
                int expandTextLength = originLayout.getLineEnd(maxLine-1) - length;
                if (expandTextLength > 0) {
                    substring = originText.substring(0, expandTextLength);
                } else {
                    Log.e(TAG, "closeExpandText: the length of openExpand over the origin text");
                    substring = originText;
                }
            }else {
                substring = originText;
            }
        }

        mTextView.setText(substring);
        if (originLayout.getLineCount()>maxLine){
            //            让span字符串生效
            mTextView.append(openExpand);
            mTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
        folded=false;
        mTextButton.setVisibility(GONE);
    }


//    public __________

    public void setMaxLine(int maxLine){
        this.maxLine=maxLine;
    }

    public void setText(String s){
        originText=s;
        folded=true;
    }

    public void setColor(int openTextColor,int closeTextColor){
        this.openTextColor=openTextColor;
        this.closeTextColor=closeTextColor;
    }

    public void setButtonColor(String openText,String closeText){
        this.openText=openText;
        this.closeText=closeText;
    }

    public void setTextSize(int textSize){
        mTextView.setTextSize(textSize);
        mTextButton.setTextSize(textSize);
    }


    class ButtonSpan extends ClickableSpan {

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
}
