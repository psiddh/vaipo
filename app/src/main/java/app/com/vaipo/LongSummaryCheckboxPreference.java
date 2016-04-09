package app.com.vaipo;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class LongSummaryCheckboxPreference extends CheckBoxPreference {
    public LongSummaryCheckboxPreference(Context ctx, AttributeSet attrs, int defStyle)
    {
        super(ctx, attrs, defStyle);
    }

    public LongSummaryCheckboxPreference(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
    }

    @Override
    protected void onBindView(View view)
    {
        super.onBindView(view);

        TextView summary= (TextView)view.findViewById(android.R.id.summary);
        summary.setMaxLines(3);
    }
}