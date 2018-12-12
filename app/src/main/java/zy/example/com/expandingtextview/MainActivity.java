package zy.example.com.expandingtextview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private FoldableTextView ftvView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ftvView=findViewById(R.id.tv_title);
        Log.i(TAG, "onCreate: foldable");
        ftvView.setMaxLine(3);
        ftvView.setText("也可以通过以下方式获取控件的宽和高也可以通过以下方式获取控件的宽和高也可以通过以下方式获取控件的宽和高也可以通过以下方式获取控件的宽和高也可以通过以下方式获取控件的宽和高也可以通过以下方式获取控件的宽和高4、onSizeChanged() 在控件大小发生改变时调用。所以这里初始化会被调用一次45678901234456789012344567890123445678901234456789012344567890123445678901234456789012344567890");
    }
}
