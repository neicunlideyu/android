package cn.onboard.android.app.widget.scrollViewWithListView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by xuchen on 14-4-28.
 */
public class ScrollViewWithListView extends ListView {


    public ScrollViewWithListView(Context context) {
        super(context);
    }

    public ScrollViewWithListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollViewWithListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE>>2,MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec,expandSpec);
    }
}
