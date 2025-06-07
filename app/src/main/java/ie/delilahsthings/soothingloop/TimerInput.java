package ie.delilahsthings.soothingloop;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class TimerInput {

    private TimerCallback callback;
    private Context context;
    private long originalValue;
    private int originalColour, placeholderColour;
    private View parentView;
    private TextView hoursInput, minutesInput, secondsInput;
    private EditText realTimeInput;

    public TimerInput(Context context, View view, TimerCallback callback, long originalValue)
    {
        this.context=context;
        this.parentView=view;
        this.callback=callback;
        this.originalValue=originalValue;

        hoursInput = view.findViewById(R.id.hours_input);
        minutesInput = view.findViewById(R.id.minutes_input);
        secondsInput = view.findViewById(R.id.seconds_input);
        realTimeInput = view.findViewById(R.id.real_time_input);
        this.originalColour = hoursInput.getCurrentTextColor();
        this.placeholderColour = parentView.getResources().getColor(R.color.gray);

        hoursInput.setOnFocusChangeListener(focusPass);
        minutesInput.setOnFocusChangeListener(focusPass);
        secondsInput.setOnFocusChangeListener(focusPass);
        realTimeInput.addTextChangedListener(onInput);
        setSeconds(originalValue);
        if(originalValue!=0) {
            setTextColour(placeholderColour);
        }
    }

    public long getSeconds()
    {
        long hours, minutes, seconds;

        try {
            hours = Long.parseLong(hoursInput.getText().toString());
        }
        catch (NumberFormatException e) {
            hours=0;
        }

        try {
            minutes = Long.parseLong(minutesInput.getText().toString());
        }
        catch (NumberFormatException e) {
            minutes=0;
        }

        try {
            seconds = Long.parseLong(secondsInput.getText().toString());
        }
        catch (NumberFormatException e) {
            seconds=0;
        }

        return (3600*hours) + (60*minutes) + seconds;
    }

    public void setSeconds(long value) {
        if(value>0) {
            secondsInput.setText(String.format("%02d", value % 60));
        }
        else {
            secondsInput.setText("");
        }
        if(value>59) {
            minutesInput.setText(String.format("%02d", ((value % 3600) / 60)));
        }
        else {
            minutesInput.setText("");
        }
        if(value>3599) {
            hoursInput.setText("" + (value / 3600));
        }
        else {
            hoursInput.setText("");
        }
    }

    public void setSeconds(String all) {
        int all_l=all.length();

        if(all_l==0) {
            setSeconds(originalValue);
            setTextColour(placeholderColour);
            return;
        }

        String hours="", minutes="", seconds="";
        if(all_l>4)
        {
            hours=all.substring(0,all_l-4);
            minutes=all.substring(all_l-4,all_l-2);
            seconds=all.substring(all_l-2);
        }

        else if(all_l>2)
        {
            minutes=all.substring(0,all_l-2);
            seconds=all.substring(all_l-2);
        }

        else
        {
            seconds=all;
        }

        hoursInput.setText(hours);
        minutesInput.setText(minutes);
        secondsInput.setText(seconds);
        setTextColour(originalColour);
    }

    void setTextColour(int colour) {
         hoursInput.setTextColor(colour);
        minutesInput.setTextColor(colour);
        secondsInput.setTextColor(colour);
    }

    final View.OnFocusChangeListener focusPass = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean focused) {
            if(focused) {
                realTimeInput.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.showSoftInput(realTimeInput, InputMethodManager.RESULT_SHOWN);
            }
        }
    };

    final TextWatcher onInput = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String all = realTimeInput.getText().toString();
            setSeconds(all);

            if(callback!=null) {
                callback.setSeconds(getSeconds());
                callback.run();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public static abstract class TimerCallback implements Runnable {
        long seconds;
        public abstract void run();

        public void setSeconds(long seconds) {
            this.seconds=seconds;
        }
    }
}
