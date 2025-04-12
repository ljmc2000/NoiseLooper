package ie.delilahsthings.soothingloop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {

    private String exportedProfileName="";
    private ViewGroup profilesView;
    private ViewGroup customSoundsView;
    private SharedPreferences settings;
    private ActivityResultLauncher<String> getNewSound, exportProfile, prepareImportProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.profilesView=findViewById(R.id.profiles);
        this.customSoundsView=findViewById(R.id.custom_sounds);
        this.getNewSound=registerForActivityResult(new ActivityResultContracts.GetContent(), this::addCustomSound);
        this.exportProfile=registerForActivityResult(new ActivityResultContracts.CreateDocument("application/xml"), this::exportProfile);
        this.prepareImportProfile=registerForActivityResult(new ActivityResultContracts.GetContent(), this::importProfile);
        this.settings=getSharedPreferences(Constants.APP_SETTINGS,MODE_MULTI_PROCESS);

        CheckboxBooleanToggle.build(settings, Constants.LOAD_DEFAULT_ON_START, findViewById(R.id.toggle_autostart));
        CheckboxBooleanToggle.build(settings, Constants.DISABLE_PROBLEM_SOUNDS, findViewById(R.id.toggle_problem_sounds), this::invalidateMainActivity);

        LinearLayout sleepTimerDuration = this.findViewById(R.id.sleep_timer_duration);
        TimerInput timerInput = new TimerInput(this, sleepTimerDuration,  new DurationChangeListener(), settings.getLong(Constants.FADEOUT_DURATION, 3));

        populateCustomProfiles();
        populateCustomSounds();
    }

    void addCustomSound(Uri uri)
    {
        try {
            CustomSoundsManager.AddedSoundResult sound=CustomSoundsManager.addCustomSound(uri);

            if(sound.size>Constants.ONE_MEGABYTE)
            {
                Toast.makeText(this,getString(R.string.big_file_warning),Toast.LENGTH_SHORT).show();
            }

            ViewGroup view = new LinearLayout(this);
            View.inflate(this, R.layout.profile_config_item, view);
            TextView text=view.findViewById(R.id.title);
            text.setText(sound.name);
            ImageView image=view.findViewById(R.id.delete_button);
            image.setOnClickListener((v)->promptDeleteCustomSound(view, sound.name));

            customSoundsView.addView(view);
            invalidateMainActivity();
        }
        catch (IOException e)
        {
            Toast.makeText(this,getString(R.string.add_sound_problem),Toast.LENGTH_SHORT).show();
        }
    }

    void invalidateMainActivity()
    {
        Intent intent = new Intent();
        intent.setAction(Constants.INVALIDATE_ACTION);
        intent.putExtra(Constants.RESTORE_VOLUMES,true);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }

    void addCustomProfile(String profileName) {
        TextView text;
        ImageView deleteButton, exportButton;

        ViewGroup view = new LinearLayout(this);
        View.inflate(this, R.layout.profile_config_item, view);
        text=view.findViewById(R.id.title);
        text.setText(profileName);
        deleteButton=view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener((v)->promptDeleteProfile(view, profileName));
        exportButton=view.findViewById(R.id.export_button);
        exportButton.setOnClickListener((View v)->promptExportProfile(profileName));

        profilesView.addView(view);
    }

    void populateCustomProfiles()
    {
        TextView text;
        ImageView deleteButton, exportButton;

        for(String profileName: ProfileManager.listProfiles()) {
            ViewGroup view = new LinearLayout(this);
            View.inflate(this, R.layout.profile_config_item, view);
            text=view.findViewById(R.id.title);
            text.setText(profileName);
            deleteButton=view.findViewById(R.id.delete_button);
            deleteButton.setOnClickListener((v)->promptDeleteProfile(view, profileName));
            exportButton=view.findViewById(R.id.export_button);
            exportButton.setOnClickListener((View v)->promptExportProfile(profileName));
            exportButton.setVisibility(View.VISIBLE);

            profilesView.addView(view);
        }
    }

    void populateCustomSounds()
    {
        TextView text;
        ImageView image;

        for(String sound: CustomSoundsManager.listCustomSounds())
        {
            ViewGroup view = new LinearLayout(this);
            View.inflate(this, R.layout.profile_config_item, view);
            text=view.findViewById(R.id.title);
            text.setText(sound);
            image=view.findViewById(R.id.delete_button);
            image.setOnClickListener((v)->promptDeleteCustomSound(view, sound));

            customSoundsView.addView(view);
        }
    }

    public void promptAddCustomSound(View view)
    {
        getNewSound.launch("audio/*");
    }

    public void promptDeleteCustomSound(View sender, String sound)
    {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(String.format(getString(R.string.confirm_delete_custom_sound),sound));
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel());
        builder.setPositiveButton(getString(R.string.confirm),(dialogInterface, i) -> {
            CustomSoundsManager.deleteCustomSound(sound);
            customSoundsView.removeView(sender);
            Intent intent = new Intent();
            intent.setAction(Constants.INVALIDATE_ACTION);
            intent.putExtra(Constants.NOISE_TO_REMOVE,sound);
            intent.putExtra(Constants.RESTORE_VOLUMES,true);
            intent.setPackage(getPackageName());
            sendBroadcast(intent);
        });

        builder.show();
    }

    public void promptImportProfile(View view) {
        prepareImportProfile.launch("text/xml");
    }

    void promptDeleteProfile(View sender, String profileName)
    {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(String.format(getString(R.string.confirm_delete_profile),profileName));
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel());
        builder.setPositiveButton(getString(R.string.confirm),(dialogInterface, i) -> {
            ProfileManager.deleteProfile(profileName);
            profilesView.removeView(sender);
        });

        builder.show();
    }

    void promptExportProfile(String profileName) {
        exportedProfileName=profileName;
        exportProfile.launch(profileName+".xml");
    }

    void exportProfile(Uri uri) {
        try {
            ProfileManager.exportProfile(exportedProfileName, uri);
        } catch (ProfileManager.ProfileIOException e) {
            Toast.makeText(this, R.string.save_profile_problem, Toast.LENGTH_SHORT).show();
        }
    }

    void importProfile(Uri uri) {
        if(uri==null) {
            return;
        }

        EditText textbox = new EditText(this);
        SaveLoadDialog saveDialog = new SaveLoadDialog(this, textbox, R.string.save_custom, R.string.save,(profileName)->{if(ProfileManager.importProfile(profileName, uri)) addCustomProfile(profileName);}, false);
        textbox.addTextChangedListener(saveDialog.getTextChangeListener());
    }

    public void restorePre1dot3Profiles(View view) {
        if(ProfileManager.migratePre1dot2Profiles()) {
            Toast.makeText(this, R.string.recovery_success, Toast.LENGTH_SHORT).show();
            profilesView.removeAllViews();
            populateCustomProfiles();
        }
        else {
            Toast.makeText(this, R.string.recovery_failure, Toast.LENGTH_SHORT).show();
        }
    }

    public void restorePre1dot3Sounds(View view) {
        if(CustomSoundsManager.migratePre1dot2Noises()) {
            Toast.makeText(this, R.string.recovery_success, Toast.LENGTH_SHORT).show();
            invalidateMainActivity();
            customSoundsView.removeAllViews();
            populateCustomSounds();
        }
        else {
            Toast.makeText(this, R.string.recovery_failure, Toast.LENGTH_SHORT).show();
        }
    }

    private class DurationChangeListener extends TimerInput.TimerCallback {
        @Override
        public void run() {
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong(Constants.FADEOUT_DURATION, seconds);
            editor.apply();
        }
    }
}