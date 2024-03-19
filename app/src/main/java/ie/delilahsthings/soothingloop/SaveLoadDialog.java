package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SaveLoadDialog implements DialogInterface.OnClickListener {

    protected Context context;
    protected View input;
    protected Listener listener;

    protected SaveLoadDialog(Context context, View input, int title, int actionName, Listener listener)
    {
        this.context=context;
        this.input=input;
        this.listener=listener;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(title);
        builder.setView(input);
        builder.setPositiveButton(actionName,this);
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel());
        builder.show();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        try {
            String profileName = "";
            if (input instanceof TextView) {
                profileName = ((TextView) input).getText().toString();
                profileName = ProfileManager.validateProfileName(profileName);
            } else if (input instanceof Spinner) {
                profileName = ((Spinner) input).getSelectedItem().toString();
            }

            listener.action(ProfileManager.prefix + profileName);
        }
        catch (ProfileManager.BadProfileNameException ex)
        {
            Toast.makeText(context, R.string.bad_profile_name_warning, Toast.LENGTH_SHORT).show();
        }
    }

    public interface Listener {
        void action(String filename);
    }
}
