package huins.ex.view.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.util.LinkedList;
import java.util.List;

import huins.ex.R;

/**
 * Created by suhak on 15. 8. 31.
 */
public class CardTextInputView extends LinearLayout implements OnEditorActionListener, View.OnFocusChangeListener {

    @Override
    public boolean onEditorAction(TextView view, int keyCode, KeyEvent event) {
        switch(view.getId()) {
            case R.id.numberInputText: {
                if (keyCode == KeyEvent.KEYCODE_ENDCALL)
                {
                    for(OnTextChangeListener listener: mTextChangeListeners) {
                        listener.onTextEditEnded(this, Double.valueOf(mNumberInputText.getText().toString()));
                    }
                    hideSoftInput();
                    return true;
                }
            }
            break;
        }
        return false;
    }

    public interface OnTextChangeListener
    {
        void onTextEditStarted(CardTextInputView view, double value);
        void onTextEditEnded(CardTextInputView view, double value);

    }


    private final List<OnTextChangeListener> mTextChangeListeners = new LinkedList<>();

    public void addEditListener(OnTextChangeListener listener) {
        mTextChangeListeners.add(listener);
    }

    public void removeEditListener(OnTextChangeListener listener) {
        mTextChangeListeners.remove(listener);
    }


    @Override
    public void onFocusChange(View view, boolean bisible) {
        switch(view.getId()) {
            case R.id.numberInputText: {

                if(bisible) {
                    showSoftInput(mNumberInputText.getText().toString());
                }
                else
                    for(OnTextChangeListener listener: mTextChangeListeners){
                        listener.onTextEditEnded(this, Double.valueOf(mNumberInputText.getText().toString()));
                    }
                hideSoftInput();

            }
            break;
        }
    }


    public CardTextInputView(Context context) {
        this(context, null);
    }

    public CardTextInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardTextInputView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    private View mVerticalDivider;
    private View mHorizontalDivider;

    private TextView mTitleView;
    private EditText mNumberInputText;

    private void initialize(final Context context, AttributeSet attrs) {
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CardTextInputView, 0, 0);

        try {
            // Setup the container view.
            setBackgroundResource(R.drawable.bg_cell_white);

            // Setup the children views
            final LayoutInflater inflater = LayoutInflater.from(context);

            // Setup the divider view
            mVerticalDivider = inflater.inflate(R.layout.card_title_vertical_divider, this, false);
            mHorizontalDivider = inflater.inflate(R.layout.card_title_horizontal_divider, this,	false);
            // Setup the title view
            mTitleView = (TextView) inflater.inflate(R.layout.card_wheel_horizontal_view_title,	this, false);
            mTitleView.setText(a.getString(R.styleable.CardWheelHorizontalView_android_text));

            final int orientation = a.getInt(R.styleable.CardWheelHorizontalView_android_orientation, VERTICAL);
            if (orientation == HORIZONTAL) {
                setOrientation(HORIZONTAL);
            } else {
                setOrientation(VERTICAL);
            }

            updateTitleLayout();

            final View textinputframe = inflater.inflate(R.layout.card_textinput_view, this, false);
            addView(textinputframe);

            mNumberInputText = (EditText) textinputframe.findViewById(R.id.numberInputText);

            mNumberInputText.setOnEditorActionListener(this);
            mNumberInputText.setOnFocusChangeListener(this);

        } finally {
            a.recycle();
        }
    }

    private void updateTitleLayout() {
        if (mTitleView == null ) {
            return;
        }

        final int childCount = getChildCount();
        if (mTitleView.length() > 0) {
            final View divider = getOrientation() == VERTICAL ? mVerticalDivider : mHorizontalDivider;

            if (childCount <= 1) {
                addView(mTitleView, 0);
                addView(divider, 1);
            } else {
                if (getChildAt(1) != divider) {
                    removeViewAt(1);
                    addView(divider, 1);
                }
            }
        } else if (childCount > 1) {
            removeViewAt(0);
            removeViewAt(1);
        }
    }

    @Override
    public void setOrientation(int orientation) {
        super.setOrientation(orientation);
        updateTitleLayout();
    }

    public void setCurrentValue(double value) {
        mNumberInputText.setText(String.valueOf(value));
    }

    public String getCurrentValue() {
        return mNumberInputText.getText().toString();
    }


    public void setText(CharSequence title) {
        mTitleView.setText(title);
        updateTitleLayout();
    }

    public void setText(int titleRes) {
        mTitleView.setText(titleRes);
        updateTitleLayout();
    }

    public CharSequence getText() {
        return mTitleView.getText();
    }

    private void showSoftInput(String currentValue) {
        final Context context = getContext();
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
                mNumberInputText.setText(currentValue);

       //     mNumberInputText.setVisibility(VISIBLE);
       //     mNumberInputText.requestFocus();
            imm.showSoftInput(mNumberInputText, 0);
        }
    }

    private void hideSoftInput() {
        final Context context = getContext();
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive(mNumberInputText)) {
            imm.hideSoftInputFromWindow(mNumberInputText.getWindowToken(), 0);
      //      mNumberInputText.setVisibility(INVISIBLE);
        }
    }
}
