package ie.delilahsthings.soothingloop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
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
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    private Bundle pausedSounds = new Bundle();
    private LinearLayout[] noise_lists;
    private LinearLayout stock_noise_list;
    private LinearLayout custom_noise_list;
    private Resources resources;
    private SharedPreferences defaultProfile, settings;

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

        defaultProfile=getSharedPreferences(Constants.DEFAULT_PROFILE,MODE_PRIVATE);
        settings=getSharedPreferences(Constants.APP_SETTINGS,MODE_MULTI_PROCESS);

        SoundEffectVolumeManager.setOnPlayCallback(()->onPlaySounds());
        populateNoiselist();
        populateCustomNoiselist();

        registerBroadcastReceivers();

        if(settings.getBoolean(Constants.LOAD_DEFAULT_ON_START,false))
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
        MenuItem playPauseButton = menu.findItem(R.id.play_pause_button);
        setPauseVisibility(playPauseButton);
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
        text.setTextAppearance(this,R.style.header);
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
        custom_noise_list.removeAllViews();

        String[] customNoises = ProfileManager.listCustomSounds(this);
        if(customNoises.length==0)
        {
            return;
        }

        //header
        TextView text = new TextView(this);
        text.setText(getString(R.string.custom_sounds));
        text.setGravity(Gravity.CENTER);
        text.setTextAppearance(this,R.style.header);
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
            volume.setTag(R.string.persist_key,Constants.CUSTOM_NOISE_PREFIX+sound);
        }
    }

    void populateNoiselist()
    {
        stock_noise_list.removeAllViews();

        try {
            Resources r=getResources();
            String pkg = getPackageName();
            String id;
            boolean ENABLE_EXTRAS = settings.getBoolean(Constants.EXTRA_NOISES, false);

            InputStream creditsFile = getResources().openRawResource(R.raw.credits);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(creditsFile);
            Element node;

            ArrayList<String> anti_sounds = new ArrayList<>();
            ArrayList<String> nature = new ArrayList<>();
            ArrayList<String> travel = new ArrayList<>();
            ArrayList<String> interiors = new ArrayList<>();

            NodeList sounds = document.getElementsByTagName("sound");
            for(int i=0; i<sounds.getLength(); i++) {
                node = (Element) sounds.item(i);
                id=node.getAttribute("id");
                if(node.hasAttribute("hide") && !ENABLE_EXTRAS)
                    continue;
                switch(node.getAttribute("class"))
                {
                    case "anti_sound":
                        anti_sounds.add(id);
                        break;
                    case "nature":
                        nature.add(id);
                        break;
                    case "travel":
                        travel.add(id);
                        break;
                    case "interiors":
                        interiors.add(id);
                        break;

                    default:
                        break;
                }
            }

            for(Pair<Integer,ArrayList<String>> _p: new Pair[]{
                    new Pair<>(R.string.header_antisound,anti_sounds),
                    new Pair<>(R.string.header_nature,nature),
                    new Pair<>(R.string.header_travel,travel),
                    new Pair<>(R.string.header_interiors,interiors),
            })
            {
                addHeader(stock_noise_list, getString(_p.first));
                for(String sound: _p.second)
                {
                    addItem(
                            r.getIdentifier(sound,"drawable",pkg),
                            r.getIdentifier(sound,"string",pkg),
                            r.getIdentifier(sound,"raw",pkg),
                            sound
                    );
                }
                addDivider();
            }
        }
        catch (ParserConfigurationException | IOException | SAXException e)
        {
            throw new RuntimeException(e);
        }
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

    private void onPlaySounds() {
        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        Menu mainMenu = mainToolbar.getMenu();
        MenuItem playPauseButton;
        try {
            playPauseButton = mainMenu.findItem(R.id.play_pause_button);
            pausedSounds.putBoolean(Constants.ANY_PLAYING, true);
            playPauseButton.setVisible(true);
            playPauseButton.setIcon(R.drawable.pause);
            playPauseButton.setTitle(R.string.pause_button_label);
        }
        catch (NullPointerException e) {
        }
    }

    public void playPause(MenuItem sender)
    {
        if(pausedSounds.getBoolean(Constants.ANY_PLAYING,true)) {
            silenceAll(sender);
            sender.setIcon(R.drawable.play_triangle);
            sender.setTitle(R.string.resume_button_label);
            pausedSounds.putBoolean(Constants.ANY_PLAYING,false);
        }

        else {
            loadState(pausedSounds);
            sender.setIcon(R.drawable.pause);
            sender.setTitle(R.string.pause_button_label);
            pausedSounds.putBoolean(Constants.ANY_PLAYING,true);
        }
    }

    public void promptLoadCustomProfile(MenuItem sender)
    {
        Spinner spinner = new Spinner(this);
        String profiles[] = ProfileManager.listProfiles(this);
        if(profiles.length!=0) {
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, profiles);
            spinner.setAdapter(spinnerArrayAdapter);
            new SaveLoadDialog(this, spinner, R.string.load_custom, R.string.load, (profileName) -> loadProfile(getSharedPreferences(profileName, MODE_PRIVATE)), true);
        }
        else {
            Toast.makeText(this,R.string.no_profiles_saved,Toast.LENGTH_SHORT).show();
        }
    }

    public void promptSaveCustomProfile(MenuItem sender)
    {
        EditText textbox = new EditText(this);
        SaveLoadDialog saveDialog = new SaveLoadDialog(this, textbox, R.string.save_custom, R.string.save,(profileName)->saveProfile(getSharedPreferences(profileName,MODE_PRIVATE)), false);
        textbox.addTextChangedListener(saveDialog.getTextChangeListener());
    }

    public void promptSleepTimer(MenuItem sender)
    {
        View view = View.inflate(this,R.layout.timespan_input,null);
        TimerInput timerInput = new TimerInput(this, view);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.sleep_timer);
        builder.setView(view);
        builder.setPositiveButton(R.string.confirm, (dialogInterface, i) -> SleepTimerThread.setTime(timerInput.getSeconds()));
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel());
        if(SleepTimerThread.isRunning())
        {
            builder.setNeutralButton(R.string.stop, (dialogInterface,i)->{
                SleepTimerThread.stopSleepTimer();
                getSupportActionBar().setTitle(R.string.app_name);
            });
        }

        builder.show();
    }

    void registerBroadcastReceivers()
    {
        //sleep timer
        BroadcastReceiver sleepTimerEvent = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long remainingTime=intent.getLongExtra(Constants.REMAINING_TIME,0);

                if(intent.getLongExtra(Constants.REMAINING_TIME,0)>0) {
                    String sep = getString(R.string.time_separator);
                    String title = String.format("%02d%s%02d%s%02d",
                            remainingTime / 3600, sep,
                            remainingTime / 60 % 60, sep,
                            remainingTime % 60, 0);
                    getSupportActionBar().setTitle(title);
                }
                else {
                    getSupportActionBar().setTitle(R.string.app_name);
                    fadeOut();
                }
            }
        };

        //custom sounds list changed
        BroadcastReceiver onNoiseListChange = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                populateNoiselist();
                populateCustomNoiselist();
            }
        };

        //headphones unplugged
        BroadcastReceiver onAudioDeviceChange=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                silenceAll();
            }
        };

        if (Build.VERSION.SDK_INT >= 26) {
            registerReceiver(sleepTimerEvent, new IntentFilter(Constants.TIMER_EVENT), Context.RECEIVER_NOT_EXPORTED);
            registerReceiver(onNoiseListChange,new IntentFilter(Constants.INVALIDATE_ACTION), Context.RECEIVER_NOT_EXPORTED);
            registerReceiver(onAudioDeviceChange,new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY), Context.RECEIVER_EXPORTED);
        }
        else
        {
            registerReceiver(sleepTimerEvent, new IntentFilter(Constants.TIMER_EVENT));
            registerReceiver(onNoiseListChange,new IntentFilter(Constants.INVALIDATE_ACTION));
            registerReceiver(onAudioDeviceChange,new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        }
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
        int volume;
        boolean anyPlaying=false;

        for(LinearLayout noise_list: noise_lists) {
            for (int i = 0; i < noise_list.getChildCount(); i++) {
                v = noise_list.getChildAt(i).findViewById(R.id.volume);
                if (v != null) {
                    persistKey = (String) v.getTag(R.string.persist_key);
                    volume=v.getProgress();
                    state.putInt(persistKey, volume);
                    if(volume!=0)
                        anyPlaying=true;
                }
            }
        }

        state.putBoolean(Constants.ANY_PLAYING,anyPlaying);
    }

    void setPauseVisibility(MenuItem playPauseButton)
    {
        playPauseButton.setVisible(SoundEffectVolumeManager.EVER_PLAYED);

        if(pausedSounds.getBoolean(Constants.ANY_PLAYING,true))
        {
            playPauseButton.setTitle(R.string.pause_button_label);
            playPauseButton.setIcon(R.drawable.pause);
        }
        else
        {
            playPauseButton.setTitle(R.string.resume_button_label);
            playPauseButton.setIcon(R.drawable.play_triangle);
        }
    }

    public void silenceAll()
    {
        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        Menu mainMenu = mainToolbar.getMenu();
        MenuItem playPauseButton;
        try {
            playPauseButton = mainMenu.findItem(R.id.play_pause_button);
            silenceAll(playPauseButton);
        }
        catch (NullPointerException e) {
        }
    }
    public void silenceAll(MenuItem playPauseButton)
    {
        if(pausedSounds.getBoolean(Constants.ANY_PLAYING,true)) {
            saveState(pausedSounds);
            playPauseButton.setIcon(R.drawable.play_triangle);
            playPauseButton.setTitle(R.string.resume_button_label);
            pausedSounds.putBoolean(Constants.ANY_PLAYING, false);
        }

        SoundEffectVolumeManager.stopAll();

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

    void fadeOut()
    {
        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        Menu mainMenu = mainToolbar.getMenu();
        MenuItem playPauseButton;
        try {
            playPauseButton = mainMenu.findItem(R.id.play_pause_button);
            if(pausedSounds.getBoolean(Constants.ANY_PLAYING,true)) {
                saveState(pausedSounds);
                playPauseButton.setIcon(R.drawable.play_triangle);
                playPauseButton.setTitle(R.string.resume_button_label);
                pausedSounds.putBoolean(Constants.ANY_PLAYING, false);
            }
        }
        catch (NullPointerException e) {
        }

        FadeOutThread fadeOutThread = new FadeOutThread(this);
        fadeOutThread.start();
    }

   static class FadeOutThread extends Thread{
        private Context context;
        public FadeOutThread(Context context)
        {
            this.context=context;
        }
        @Override
        public void run()
        {
            SoundEffectVolumeManager.fadeOut(3000, context);
        }
    }
}