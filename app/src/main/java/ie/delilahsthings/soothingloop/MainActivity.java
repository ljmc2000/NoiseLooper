package ie.delilahsthings.soothingloop;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private LinearLayout noise_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noise_list=this.findViewById(R.id.noise_list);

        populateNoiselist();
    }

    void addDivider()
    {
        ViewGroup view = new LinearLayout(this);
        View.inflate(this, R.layout.hline, view);
        noise_list.addView(view);
    }

    void addHeader(String name)
    {
        TextView text = new TextView(this);
        text.setText(name);
        text.setGravity(Gravity.CENTER);
        text.setTextSize(38);
        text.setPadding(0,0,0,10);
        noise_list.addView(text);
    }

    void addItem(String name)
    {
        ViewGroup view = new LinearLayout(this);
        View.inflate(this, R.layout.noise_config_item, view);
        noise_list.addView(view);

        ((TextView) view.findViewById(R.id.noise_name)).setText(name);
    }

    void populateNoiselist()
    {
        addHeader("Anti Sound");
        addItem("Brownian Noise");
        addItem("Pink Noise");
        addItem("White Noise");
        addDivider();

        addHeader("Nature");
        addItem("Rain");
        addItem("Storm");
        addItem("Wind");
        addItem("Waves");
        addItem("Stream");
        addItem("Birds");
        addItem("Summer Night");
        addDivider();

        addHeader("Travel");
        addItem("Train");
        addItem("Boat");
        addItem("City");
        addDivider();

        addHeader("Interiors");
        addItem("Coffee Shop");
        addItem("Fireplace");
        addDivider();
    }
}