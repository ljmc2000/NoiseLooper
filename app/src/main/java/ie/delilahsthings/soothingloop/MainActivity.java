package ie.delilahsthings.soothingloop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioAttributes;
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

    private AudioManager audioManager;
    SharedPreferences defaultProfile;
    private LinearLayout noise_list;
    private Resources resources;
    private ArrayList<SoundEffectVolumeManager> managers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        noise_list=this.findViewById(R.id.noise_list);
        resources=getResources();

        audioManager=(AudioManager) getSystemService(Context.AUDIO_SERVICE);
        defaultProfile=getSharedPreferences("default",MODE_PRIVATE);
        if(SoundEffectVolumeManager.soundPool!=null)
        {
            SoundEffectVolumeManager.soundPool.release();
        }
        SoundEffectVolumeManager.soundPool=new SoundPool(32, AudioManager.STREAM_MUSIC,0);

        populateNoiselist();
        loadProfile(defaultProfile);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        loadProfile(defaultProfile);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveProfile(defaultProfile);
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

    void addItem(int iconId, int nameId, int soundId, String persistKey)
    {
        ViewGroup view = new LinearLayout(this);
        View.inflate(this, R.layout.noise_config_item, view);
        noise_list.addView(view);

        TextView noiseName = view.findViewById(R.id.noise_name);
        noiseName.setText(resources.getString(nameId));
        ImageView icon = view.findViewById(R.id.icon);
        icon.setImageDrawable(resources.getDrawable(iconId));
        SeekBar volume = view.findViewById(R.id.volume);
        SoundEffectVolumeManager manager=new SoundEffectVolumeManager(getBaseContext(),soundId);
        managers.add(manager);
        volume.setOnSeekBarChangeListener(manager);
        volume.setTag(R.string.persist_key,persistKey);
    }

    void populateNoiselist()
    {
        addHeader(resources.getString(R.string.header_antisound));
        //addItem(R.drawable.brown_noise,R.string.brown_noise,R.raw.brown_noise,"brown_noise");   //unapproved upstream, for personal use
        addItem(R.drawable.pink_noise,R.string.pink_noise,R.raw.pink_noise,"pink_noise");
        addItem(R.drawable.white_noise,R.string.white_noise,R.raw.white_noise, "white_noise");
        addDivider();

        addHeader(resources.getString(R.string.header_nature));
        addItem(R.drawable.rain,R.string.rain,R.raw.rain,"rain");
        addItem(R.drawable.storm,R.string.storm,R.raw.storm,"storm");
        addItem(R.drawable.wind,R.string.wind,R.raw.wind,"wind");
        addItem(R.drawable.waves,R.string.waves,R.raw.waves,"waves");
        addItem(R.drawable.stream,R.string.stream,R.raw.stream,"stream");
        addItem(R.drawable.birds,R.string.birds,R.raw.birds,"birds");
        addItem(R.drawable.summer_night,R.string.summer_night,R.raw.summer_night,"summer_night");

        addDivider();

        addHeader(resources.getString(R.string.header_travel));
        addItem(R.drawable.train,R.string.train,R.raw.train,"train");
        addItem(R.drawable.boat,R.string.boat,R.raw.boat,"boat");
        addItem(R.drawable.city,R.string.city,R.raw.city,"city");
        addDivider();

        addHeader(resources.getString(R.string.header_interiors));
        addItem(R.drawable.coffee_shop,R.string.coffee_shop,R.raw.coffee_shop,"coffee_shop");
        addItem(R.drawable.fireplace,R.string.fireplace,R.raw.fireplace,"fireplace");
        addDivider();
    }

    public void displayCredits(MenuItem sender)
    {
        Intent showCredits = new Intent(this, CreditsActivity.class);
        startActivity(showCredits);
    }

    public void loadDefaults(MenuItem sender){
        loadProfile(defaultProfile);
    }

    private void loadProfile(SharedPreferences profile)
    {
        SeekBar v;
        String persistKey;

        for(int i=0; i<noise_list.getChildCount(); i++)
        {
            v=noise_list.getChildAt(i).findViewById(R.id.volume);
            if(v!=null)
            {
                persistKey=(String) v.getTag(R.string.persist_key);
                v.setProgress(profile.getInt(persistKey,0));
            }
        }
    }

    public void promptLoadCustomProfile(MenuItem sender)
    {
        ArrayList<String> files=new ArrayList<>();
        for(String file: SaveLoadDialog.listProfiles(this))
        {
            if(file.startsWith(SaveLoadDialog.prefix))
                files.add(file.substring(SaveLoadDialog.prefix_length,file.length()-4));
        }

        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, files);
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

        for(int i=0; i<noise_list.getChildCount(); i++)
        {
            v=noise_list.getChildAt(i).findViewById(R.id.volume);
            if(v!=null)
            {
                persistKey=(String) v.getTag(R.string.persist_key);
                editor.putInt(persistKey,v.getProgress());
            }
        }

        editor.commit();
    }

    public void silenceAll(MenuItem sender)
    {
        saveProfile(defaultProfile);
        silenceAll();
    }

    public void silenceAll()
    {
        SeekBar v;

        for(int i=0; i<noise_list.getChildCount(); i++)
        {
            v=noise_list.getChildAt(i).findViewById(R.id.volume);
            if(v!=null)
            {
                v.setProgress(0);
            }
        }
    }
}