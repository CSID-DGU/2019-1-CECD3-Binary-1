package huins.ex.proto.display;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suhak on 15. 6. 26.
 */
public class StatusButton extends RelativeLayout implements View.OnClickListener
{

    public List<String> getDisplaymasure() {
        return displaymasure;
    }

    private  List<String> displaymasure;

    TextView _text1;
    TextView _text2;

    public int getIndex() {
        return index;
    }

    private  int index;

    public StatusButton(Context context)
    {
        super(context);

        displaymasure = new ArrayList<String>();

        index = 0;

        _text1 = new TextView(context);
        _text2 = new TextView(context);


        _text1.setTextSize(16);
        _text1.setPadding(5, 3, 0, 3);
        _text1.setTypeface(Typeface.DEFAULT_BOLD);
        _text1.setBackgroundColor(Color.RED);
        _text1.setGravity(Gravity.LEFT | Gravity.CENTER);

        _text2.setTextSize(12);
        _text2.setPadding(5, 3, 0, 3);
        _text2.setTypeface(Typeface.DEFAULT_BOLD);
        _text2.setBackgroundColor(Color.RED);
        _text2.setGravity(Gravity.LEFT | Gravity.CENTER);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT | RelativeLayout.ALIGN_PARENT_TOP , RelativeLayout.TRUE);

        this.addView(_text1, params);

        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

        this.addView(_text2, params);

        this.setBackgroundColor(Color.BLUE);
        this.setOnClickListener(this);

    }

    public void setText(CharSequence text, String index)
    {
        _text1.setText(text);
        _text2.setText(index);

    }

    public void SetListType(List<String> statuslist)
    {
        displaymasure.addAll(statuslist);
        Log.d("sdgsdf", String.valueOf(displaymasure.size()));

        setText(getDisplaymasure().get(index).toString(), String.valueOf(index));
    }

    @Override
    public void onClick(View view) {

    index = ++index % displaymasure.size();
    setText( getDisplaymasure().get(index).toString(), String.valueOf(index));
    }

}

