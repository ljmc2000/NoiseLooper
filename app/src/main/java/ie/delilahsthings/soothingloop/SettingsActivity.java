package ie.delilahsthings.soothingloop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

    private ViewGroup profilesView;
    private ViewGroup customSoundsView;
    private SharedPreferences settings;
    private ActivityResultLauncher<String> getNewSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.profilesView=findViewById(R.id.profiles);
        this.customSoundsView=findViewById(R.id.custom_sounds);
        this.getNewSound=registerForActivityResult(new ActivityResultContracts.GetContent(), (uri)->addCustomSound(uri));
        this.settings=getSharedPreferences(Constants.APP_SETTINGS,MODE_MULTI_PROCESS);

        CheckBox loadOnStart = ((CheckBox)findViewById(R.id.toggle_autostart));
        loadOnStart.setChecked(settings.getBoolean(Constants.LOAD_DEFAULT_ON_START, false));
        loadOnStart.setOnCheckedChangeListener((box,checked)->toggleLoadDefaultOnStart(box,checked));
        populateCustomProfiles();
        populateCustomSounds();
    }

    void addCustomSound(Uri uri)
    {
        try {
            String sound=ProfileManager.addCustomSound(this,uri);
            ViewGroup view = new LinearLayout(this);
            View.inflate(this, R.layout.profile_config_item, view);
            TextView text=view.findViewById(R.id.title);
            text.setText(sound);
            ImageView image=view.findViewById(R.id.delete_button);
            image.setOnClickListener((v)->promptDeleteCustomSound(view, sound));

            customSoundsView.addView(view);
            Intent intent = new Intent();
            intent.setAction(Constants.INVALIDATE_ACTION);
            sendBroadcast(intent);
        }
        catch (IOException e)
        {
            Toast.makeText(this,getString(R.string.add_sound_problem),Toast.LENGTH_SHORT).show();
        }
    }

    void populateCustomProfiles()
    {
        TextView text;
        ImageView image;

        for(String profileName: ProfileManager.listProfiles(this)) {
            ViewGroup view = new LinearLayout(this);
            View.inflate(this, R.layout.profile_config_item, view);
            text=view.findViewById(R.id.title);
            text.setText(profileName);
            image=view.findViewById(R.id.delete_button);
            image.setOnClickListener((v)->promptDeleteProfile(view, profileName));

            profilesView.addView(view);
        }
    }

    void populateCustomSounds()
    {
        TextView text;
        ImageView image;

        for(String sound: ProfileManager.listCustomSounds(this))
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
            ProfileManager.deleteCustomSound(this,sound);
            customSoundsView.removeView(sender);
            Intent intent = new Intent();
            intent.setAction(Constants.INVALIDATE_ACTION);
            sendBroadcast(intent);
        });

        builder.show();
    }

    void promptDeleteProfile(View sender, String profileName)
    {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(String.format(getString(R.string.confirm_delete_profile),profileName));
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel());
        builder.setPositiveButton(getString(R.string.confirm),(dialogInterface, i) -> {
            ProfileManager.deleteProfile(this,profileName);
            profilesView.removeView(sender);
        });

        builder.show();
    }

    public void toggleLoadDefaultOnStart(CompoundButton box, boolean checked)
    {
        SharedPreferences.Editor editor=settings.edit();
        editor.putBoolean(Constants.LOAD_DEFAULT_ON_START,checked);
        editor.commit();
    }

    public void toggleLoadDefaultOnStart(View sender)
    {
        CompoundButton box = sender.findViewById(R.id.toggle_autostart);
        boolean checked = !box.isChecked();
        box.toggle();
        toggleLoadDefaultOnStart(box,checked);
    }
}