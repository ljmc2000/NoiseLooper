package ie.delilahsthings.soothingloop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SharedPreferences defaultProfile, preferences;
    private LinearLayout[] noise_lists;
    private LinearLayout stock_noise_list;
    private LinearLayout custom_noise_list;
    private Resources resources;
    private static int TEXT_SIZE=38;
    final static String CUSTOM_NOISE_PREFIX="custom_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        stock_noise_list=this.findViewById(R.id.stock_noise_list);
        custom_noise_list=this.findViewById(R.id.custom_noise_list);
        noise_lists=new LinearLayout[]{stock_noise_list,custom_noise_list};
        resources=getResources();

        defaultProfile=getSharedPreferences("default",MODE_PRIVATE);
        preferences=getSharedPreferences("preferences",MODE_PRIVATE);

        populateNoiselist();
        populateCustomNoiselist();

        if(preferences.getBoolean("load_default_on_start",false))
        {
            loadProfile(defaultProfile);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        loadState(savedInstanceState);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    void addDivider()
    {
        ViewGroup view = new LinearLayout(this);
        View.inflate(this, R.layout.hline, view);
        stock_noise_list.addView(view);
    }

    void addHeader(ViewGroup noise_list, String name)
    {
        TextView text = new TextView(this);
        text.setText(name);
        text.setGravity(Gravity.CENTER);
        text.setTextSize(TEXT_SIZE);
        text.setPadding(0,0,0,10);
        noise_list.addView(text);
    }

    void addItem(int iconId, int nameId, int soundId, String persistKey)
    {
        ViewGroup view = new LinearLayout(this);
        View.inflate(this, R.layout.noise_config_item, view);
        stock_noise_list.addView(view);

        TextView noiseName = view.findViewById(R.id.noise_name);
        noiseName.setText(resources.getString(nameId));
        ImageView icon = view.findViewById(R.id.icon);
        icon.setImageDrawable(resources.getDrawable(iconId));
        SeekBar volume = view.findViewById(R.id.volume);
        SoundEffectVolumeManager manager=SoundEffectVolumeManager.get(getBaseContext(),persistKey,soundId);
        volume.setOnSeekBarChangeListener(manager);
        volume.setTag(R.string.persist_key,persistKey);
    }

    void populateCustomNoiselist()
    {
        String[] customNoises = ProfileManager.listCustomSounds(this);
        if(customNoises.length==0)
        {
            return;
        }

        //header
        TextView text = new TextView(this);
        text.setText(getString(R.string.custom_sounds));
        text.setGravity(Gravity.CENTER);
        text.setTextSize(TEXT_SIZE);
        text.setPadding(0,0,0,10);
        custom_noise_list.addView(text);

        ViewGroup view;
        for(String sound: customNoises)
        {
            view = new LinearLayout(this);
            View.inflate(this, R.layout.noise_config_item, view);
            custom_noise_list.addView(view);

            TextView noiseName = view.findViewById(R.id.noise_name);
            noiseName.setText(sound);
            SeekBar volume = view.findViewById(R.id.volume);
            SoundEffectVolumeManager manager=SoundEffectVolumeManager.get(ProfileManager.getSoundPath(this)+sound);
            volume.setOnSeekBarChangeListener(manager);
            volume.setTag(R.string.persist_key,CUSTOM_NOISE_PREFIX+sound);
        }
    }

    void populateNoiselist()
    {
        addHeader(stock_noise_list,resources.getString(R.string.header_antisound));
        //addItem(R.drawable.brown_noise,R.string.brown_noise,R.raw.brown_noise,"brown_noise");   //unapproved upstream, for personal use
        addItem(R.drawable.pink_noise,R.string.pink_noise,R.raw.pink_noise,"pink_noise");
        addItem(R.drawable.white_noise,R.string.white_noise,R.raw.white_noise, "white_noise");
        addDivider();

        addHeader(stock_noise_list,resources.getString(R.string.header_nature));
        addItem(R.drawable.rain,R.string.rain,R.raw.rain,"rain");
        addItem(R.drawable.storm,R.string.storm,R.raw.storm,"storm");
        addItem(R.drawable.wind,R.string.wind,R.raw.wind,"wind");
        addItem(R.drawable.waves,R.string.waves,R.raw.waves,"waves");
        addItem(R.drawable.stream,R.string.stream,R.raw.stream,"stream");
        addItem(R.drawable.birds,R.string.birds,R.raw.birds,"birds");
        addItem(R.drawable.summer_night,R.string.summer_night,R.raw.summer_night,"summer_night");

        addDivider();

        addHeader(stock_noise_list,resources.getString(R.string.header_travel));
        addItem(R.drawable.train,R.string.train,R.raw.train,"train");
        addItem(R.drawable.boat,R.string.boat,R.raw.boat,"boat");
        addItem(R.drawable.city,R.string.city,R.raw.city,"city");
        addDivider();

        addHeader(stock_noise_list,resources.getString(R.string.header_interiors));
        addItem(R.drawable.coffee_shop,R.string.coffee_shop,R.raw.coffee_shop,"coffee_shop");
        addItem(R.drawable.fireplace,R.string.fireplace,R.raw.fireplace,"fireplace");
        addDivider();
    }

    public void displayCredits(MenuItem sender)
    {
        Intent showCredits = new Intent(this, CreditsActivity.class);
        startActivity(showCredits);
    }

    public void displaySettings(MenuItem sender)
    {
        Intent showCredits = new Intent(this, SettingsActivity.class);
        startActivity(showCredits);
    }

    public void loadDefaults(MenuItem sender){
        loadProfile(defaultProfile);
    }

    private void loadProfile(SharedPreferences profile)
    {
        SeekBar v;
        String persistKey;

        for(ViewGroup noise_list: noise_lists) {
            for (int i = 0; i < noise_list.getChildCount(); i++) {
                v = noise_list.getChildAt(i).findViewById(R.id.volume);
                if (v != null) {
                    persistKey = (String) v.getTag(R.string.persist_key);
                    v.setProgress(profile.getInt(persistKey, 0));
                }
            }
        }
    }

    private void loadState(Bundle state)
    {
        SeekBar v;
        String persistKey;

        for(ViewGroup noise_list: noise_lists) {
            for (int i = 0; i < noise_list.getChildCount(); i++) {
                v = noise_list.getChildAt(i).findViewById(R.id.volume);
                if (v != null) {
                    persistKey = (String) v.getTag(R.string.persist_key);
                    v.setProgress(state.getInt(persistKey, 0));
                }
            }
        }
    }

    public void promptLoadCustomProfile(MenuItem sender)
    {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ProfileManager.listProfiles(this));
        spinner.setAdapter(spinnerArrayAdapter);
        new SaveLoadDialog(this, spinner, R.string.load_custom, R.string.load,(profileName)->loadProfile(getSharedPreferences(profileName,MODE_PRIVATE)));
    }

    public void promptSaveCustomProfile(MenuItem sender)
    {
        new SaveLoadDialog(this, new EditText(this), R.string.save_custom, R.string.save,(profileName)->saveProfile(getSharedPreferences(profileName,MODE_PRIVATE)));
    }

    public void saveDefaults(MenuItem sender)
    {
        saveProfile(defaultProfile);
    }

    public void saveProfile(SharedPreferences profile)
    {
        SeekBar v;
        String persistKey;
        SharedPreferences.Editor editor = profile.edit();

        for(LinearLayout noise_list: noise_lists) {
            for (int i = 0; i < noise_list.getChildCount(); i++) {
                v = noise_list.getChildAt(i).findViewById(R.id.volume);
                if (v != null) {
                    persistKey = (String) v.getTag(R.string.persist_key);
                    editor.putInt(persistKey, v.getProgress());
                }
            }
        }

        editor.commit();
    }

    public void saveState(Bundle state)
    {
        SeekBar v;
        String persistKey;

        for(LinearLayout noise_list: noise_lists) {
            for (int i = 0; i < noise_list.getChildCount(); i++) {
                v = noise_list.getChildAt(i).findViewById(R.id.volume);
                if (v != null) {
                    persistKey = (String) v.getTag(R.string.persist_key);
                    state.putInt(persistKey, v.getProgress());
                }
            }
        }
    }

    public void silenceAll(MenuItem sender)
    {
        saveProfile(defaultProfile);
        silenceAll();
    }

    public void silenceAll()
    {
        SeekBar v;

        for(LinearLayout noise_list: noise_lists) {
            for (int i = 0; i < noise_list.getChildCount(); i++) {
                v = noise_list.getChildAt(i).findViewById(R.id.volume);
                if (v != null) {
                    v.setProgress(0);
                }
            }
        }
    }
}