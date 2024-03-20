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

    private Context context;
    private View parentView;
    private TextView hoursInput, minutesInput, secondsInput;
    private EditText realTimeInput;

    public TimerInput(Context context, View view)
    {
        this.context=context;
        this.parentView=view;

        hoursInput = view.findViewById(R.id.hours_input);
        minutesInput = view.findViewById(R.id.minutes_input);
        secondsInput = view.findViewById(R.id.seconds_input);
        realTimeInput = view.findViewById(R.id.real_time_input);

        hoursInput.setOnFocusChangeListener(focusPass);
        minutesInput.setOnFocusChangeListener(focusPass);
        secondsInput.setOnFocusChangeListener(focusPass);
        realTimeInput.addTextChangedListener(onInput);
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
            int all_l=all.length();

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
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
}
