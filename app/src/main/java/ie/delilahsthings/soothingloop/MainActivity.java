package ie.delilahsthings.soothingloop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private LinearLayout noise_list;
    private Resources resources;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resources=getResources();
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

    void addItem(int icon_id, int name_id, int sound_id)
    {
        ViewGroup view = new LinearLayout(this);
        View.inflate(this, R.layout.noise_config_item, view);
        noise_list.addView(view);

        TextView noiseName = view.findViewById(R.id.noise_name);
        noiseName.setText(resources.getString(name_id));
        ImageView icon = view.findViewById(R.id.icon);
        icon.setImageDrawable(resources.getDrawable(icon_id));
    }

    void populateNoiselist()
    {
        addHeader(resources.getString(R.string.header_antisound));
        addItem(R.drawable.brown_noise,R.string.brown_noise,R.raw.brown_noise);
        addItem(R.drawable.pink_noise,R.string.pink_noise,R.raw.pink_noise);
        addItem(R.drawable.white_noise,R.string.white_noise,R.raw.white_noise);
        addDivider();

        addHeader(resources.getString(R.string.header_nature));
        addItem(R.drawable.rain,R.string.rain,R.raw.rain);
        addItem(R.drawable.storm,R.string.storm,R.raw.storm);
        addItem(R.drawable.wind,R.string.wind,R.raw.wind);
        addItem(R.drawable.waves,R.string.waves,R.raw.waves);
        addItem(R.drawable.stream,R.string.stream,R.raw.stream);
        addItem(R.drawable.birds,R.string.birds,R.raw.birds);
        addItem(R.drawable.summer_night,R.string.summer_night,R.raw.summer_night);

        addDivider();

        addHeader(resources.getString(R.string.header_travel));
        addItem(R.drawable.train,R.string.train,R.raw.train);
        addItem(R.drawable.boat,R.string.boat,R.raw.boat);
        addItem(R.drawable.city,R.string.city,R.raw.city);
        addDivider();

        addHeader(resources.getString(R.string.header_interiors));
        addItem(R.drawable.coffee_shop,R.string.coffee_shop,R.raw.coffee_shop);
        addItem(R.drawable.fireplace,R.string.fireplace,R.raw.fireplace);
        addDivider();
    }
}