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
    private SharedPreferences settings;

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

        settings=getSharedPreferences(Constants.APP_SETTINGS,MODE_MULTI_PROCESS);

        Util.run_once(settings, CustomSoundsManager::migratePre1dot2Noises, Constants.PRE_1DOT3_NOISE_MIGRATION_COMPLETE);
        Util.run_once(settings, ProfileManager::migratePre1dot2Profiles, Constants.PRE_1DOT3_PROFILE_MIGRATION_COMPLETE);

        SoundEffectVolumeManager.setOnPlayCallback(this::onPlaySounds);
        populateNoiselist();
        populateCustomNoiselist();

        registerBroadcastReceivers();

        if(settings.getBoolean(Constants.LOAD_DEFAULT_ON_START,false))
        {
            applyDefaultProfile(null);
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
        MenuItem profilesMenuButton = menu.findItem(R.id.load_profile_button);
        setupProfilesMenu(profilesMenuButton);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem profilesMenuButton = menu.findItem(R.id.load_profile_button);
        setupProfilesMenu(profilesMenuButton);
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

        String[] customNoises = CustomSoundsManager.listCustomSounds();
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
            SoundEffectVolumeManager manager=SoundEffectVolumeManager.get(CustomSoundsManager.getSoundPath()+sound);
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
            boolean DISABLE_PROBLEM_SOUNDS = settings.getBoolean(Constants.DISABLE_PROBLEM_SOUNDS, false);

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
                if(DISABLE_PROBLEM_SOUNDS && node.hasAttribute("hide"))
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

    public void applyDefaultProfile(MenuItem sender){
        applyProfile(ProfileManager.loadDefaultProfile());
    }

    private void applyProfile(ProfileManager.Profile profile)
    {
        SeekBar v;
        String persistKey;

        for(ViewGroup noise_list: noise_lists) {
            for (int i = 0; i < noise_list.getChildCount(); i++) {
                v = noise_list.getChildAt(i).findViewById(R.id.volume);
                if (v != null) {
                    persistKey = (String) v.getTag(R.string.persist_key);
                    v.setProgress(profile.get(persistKey));
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

    public boolean loadCustomProfile(MenuItem sender)
    {
        try {
            applyProfile(ProfileManager.loadProfile(sender.getTitle().toString()));
        } catch (ProfileManager.ProfileLoadException e) {
            Toast.makeText(this, R.string.load_profile_problem, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void promptSaveCustomProfile(MenuItem sender)
    {
        EditText textbox = new EditText(this);
        SaveLoadDialog saveDialog = new SaveLoadDialog(this, textbox, R.string.save_custom, R.string.save,(profileName)-> {
            try {
                ProfileManager.saveProfile(profileName, pickleProfile());
                invalidateOptionsMenu();
            } catch (ProfileManager.ProfileSaveException e) {
                Toast.makeText(this, R.string.save_profile_problem, Toast.LENGTH_SHORT).show();
            }
        }, false);
        textbox.addTextChangedListener(saveDialog.getTextChangeListener());
    }

    public void promptSleepTimer(MenuItem sender)
    {
        View view = View.inflate(this,R.layout.timespan_input,null);
        TimerInput timerInput = new TimerInput(this, view, null, settings.getLong(Constants.LAST_TIMER_VALUE, 0));
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.sleep_timer);
        builder.setView(view);
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel());
        builder.setPositiveButton(R.string.confirm, (dialogInterface, i) -> {
            SleepTimerThread.setTime(timerInput.getSeconds());
            SharedPreferences.Editor edit = settings.edit();
            edit.putLong(Constants.LAST_TIMER_VALUE, timerInput.getSeconds());
            edit.commit();
        });
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
        BroadcastReceiver fadeoutEvent = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean interrupted = intent.getBooleanExtra(Constants.FADEOUT_INTERRUPTED, false);
                if(interrupted)
                    silenceAll();
            }
        };

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
                boolean restoreVolumes = intent.getBooleanExtra(Constants.RESTORE_VOLUMES,false);

                if(restoreVolumes)
                    saveState(pausedSounds);

                populateNoiselist();
                populateCustomNoiselist();
                String noise_to_remove = intent.getStringExtra(Constants.NOISE_TO_REMOVE);
                if(noise_to_remove!=null)
                    SoundEffectVolumeManager.unload(CustomSoundsManager.getSoundPath()+noise_to_remove);
                if(restoreVolumes)
                    loadState(pausedSounds);
            }
        };

        //custom profiles added or removed
        BroadcastReceiver onProfileAddedOrRemoved=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                invalidateOptionsMenu();
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
            registerReceiver(fadeoutEvent, new IntentFilter(Constants.FADEOUT_ACTION), Context.RECEIVER_NOT_EXPORTED);
            registerReceiver(sleepTimerEvent, new IntentFilter(Constants.TIMER_EVENT), Context.RECEIVER_NOT_EXPORTED);
            registerReceiver(onNoiseListChange,new IntentFilter(Constants.INVALIDATE_ACTION), Context.RECEIVER_NOT_EXPORTED);
            registerReceiver(onProfileAddedOrRemoved,new IntentFilter(Constants.INVALIDATE_PROFILES), Context.RECEIVER_NOT_EXPORTED);
            registerReceiver(onAudioDeviceChange,new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY), Context.RECEIVER_EXPORTED);
        }
        else
        {
            registerReceiver(fadeoutEvent, new IntentFilter(Constants.FADEOUT_ACTION));
            registerReceiver(sleepTimerEvent, new IntentFilter(Constants.TIMER_EVENT));
            registerReceiver(onNoiseListChange,new IntentFilter(Constants.INVALIDATE_ACTION));
            registerReceiver(onProfileAddedOrRemoved,new IntentFilter(Constants.INVALIDATE_PROFILES));
            registerReceiver(onAudioDeviceChange,new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        }
    }

    public void saveDefaults(MenuItem sender)
    {
        try {
            ProfileManager.saveDefaultProfile(pickleProfile());
        } catch (ProfileManager.ProfileSaveException e) {
            Toast.makeText(this, R.string.save_profile_problem, Toast.LENGTH_SHORT).show();
        }
    }

    private ProfileManager.Profile pickleProfile()
    {
        SeekBar v;
        String persistKey;
        ProfileManager.Profile profile = new ProfileManager.Profile();

        for(LinearLayout noise_list: noise_lists) {
            for (int i = 0; i < noise_list.getChildCount(); i++) {
                v = noise_list.getChildAt(i).findViewById(R.id.volume);
                if (v != null) {
                    persistKey = (String) v.getTag(R.string.persist_key);
                    profile.put(persistKey, v.getProgress());
                }
            }
        }

        return profile;
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

    void setupProfilesMenu(MenuItem profilesMenuButton) {
        String profiles[] = ProfileManager.listProfiles();
        Menu menu = profilesMenuButton.getSubMenu();
        MenuItem item;

        if(profiles.length!=0) {
            menu.clear();
            for(String profile: profiles) {
                item = menu.add(profile);
                item.setOnMenuItemClickListener(this::loadCustomProfile);
            }

            profilesMenuButton.setVisible(true);
        }
        else {
            profilesMenuButton.setVisible(false);
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

        populateNoiselist();
        populateCustomNoiselist();
        SoundEffectVolumeManager.fadeOut(this, settings.getLong(Constants.FADEOUT_DURATION, 3)*1000);
    }
}