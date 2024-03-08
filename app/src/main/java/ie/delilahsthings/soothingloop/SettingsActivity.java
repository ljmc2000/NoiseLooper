package ie.delilahsthings.soothingloop;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    ViewGroup profileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.profileList=findViewById(R.id.profiles);
        populateCustomProfiles();
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

            profileList.addView(view);
        }
    }

    void promptDeleteProfile(View sender, String profileName)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(String.format(getString(R.string.confirm_delete_profile),profileName));
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel());
        builder.setPositiveButton(getString(R.string.confirm),(dialogInterface, i) -> {
            ProfileManager.deleteProfile(this,profileName);
            profileList.removeView(sender);
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        Workarounds.fixBadDialogButtonColours(this, dialog);
    }
}