package ie.delilahsthings.soothingloop;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SaveLoadDialog implements DialogInterface.OnClickListener {

    protected AlertDialog dialog;
    protected Context context;
    protected View input;
    protected Listener listener;

    protected SaveLoadDialog(Context context, View input, int title, int actionName, Listener listener, boolean defaultReady)
    {
        this.context=context;
        this.input=input;
        this.listener=listener;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(title);
        builder.setView(input);
        builder.setPositiveButton(actionName,this);
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel());
        this.dialog = builder.show();

        if(!defaultReady)
        {
            Button posButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            posButton.setVisibility(View.INVISIBLE);
            posButton.setEnabled(false);
        }
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

    private static class TextChangeListener implements TextWatcher
    {
        private AlertDialog dialog;

        protected TextChangeListener(AlertDialog dialog)
        {
            this.dialog=dialog;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence profileName, int start, int count, int after) {
            Button posButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            try {
                ProfileManager.validateProfileName(profileName);
                posButton.setVisibility(View.VISIBLE);
                posButton.setEnabled(true);
            } catch (ProfileManager.BadProfileNameException e) {
                posButton.setVisibility(View.INVISIBLE);
                posButton.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    public TextWatcher getTextChangeListener()
    {
        return new TextChangeListener(dialog);
    }
}
