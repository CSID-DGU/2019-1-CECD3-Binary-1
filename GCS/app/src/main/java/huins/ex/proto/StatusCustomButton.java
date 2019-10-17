package huins.ex.proto;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by suhak on 15. 8. 4.
 */
public class StatusCustomButton extends LinearLayout implements OnClickListener {


    protected List<String> _mmeasure;
    private String _strtitle;
    private TextView _mtitle;
    private TextView _mvalue;

    protected int index = 0;

    private Context _context;

    public StatusCustomButton(Context context) {
        super(context);

        this._context = context;
    }

    public StatusCustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        this._context = context;
    }


    public StatusCustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this._context = context;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StatusCustomButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        this._context = context;
    }


    //public set params
    public void setTitleColor(int color) {
        _mtitle.setTextColor(color);
    }

    public void setValueColor(int color) {
        _mvalue.setTextColor(color);
    }

    public void setTitleSize(int size) {
        _mtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setValueSize(int size) {
        _mvalue.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setTitle(String title) {
        _strtitle = title;
    }

    public void setValue(String value) {
        _mvalue.setText(value);
    }


    public void set_mmeasure(List<String> _mmeasure) {
        this._mmeasure = _mmeasure;
    }

    private void initialize() {
        LinearLayout.LayoutParams llp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        llp.setMargins(15,0,0,0);
        _mtitle = new TextView(_context);
        _mtitle.setLayoutParams(llp);
        this.addView(_mtitle);

        _mvalue = new TextView(_context);
        _mvalue.setLayoutParams(llp);
        this.addView(_mvalue);


        _mtitle.setClickable(true);
        _mtitle.setFocusable(false);
        _mtitle.setFocusableInTouchMode(false);

        _mvalue.setClickable(true);
        _mvalue.setFocusable(false);
        _mvalue.setFocusableInTouchMode(false);


        this.setClickable(true);
        this.setFocusable(true);
        this.setFocusableInTouchMode(false);

      //  Drawable drawable = getResources().getDrawable(R.drawable.buttonoutline);
      //  this.setBackground(drawable);

        this.setBackgroundColor(Color.parseColor("#5D5D5D"));

        setTitleColor(Color.WHITE);
        setValueColor(Color.WHITE);

        setTitleSize(16);
        setValueSize(16);

        _mtitle.setText(_strtitle);
        setValue("- -");

        _mtitle.setOnClickListener(this);
        _mvalue.setOnClickListener(this);
    }

    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)this.getLayoutParams();
        this.setOrientation(LinearLayout.VERTICAL);

        this.setLayoutParams(params);

        initialize();
    }

    protected void updateUI()
    {
    }

    @Override
    public void onClick(View view) {

        if(_mmeasure != null && _mmeasure.size() > 0)
        {
            index = (++index) % _mmeasure.size();
        }

        updateUI();

    }
}
